package com.anyline.examples.scanViewConfig

import android.content.Context
import java.io.File
import java.io.IOException

data class ScanViewConfigFolder(val assetFolderName: String,
                                val friendlyName: String,
                                val parent: ScanViewConfigFolder?) {

    private val folderList = mutableListOf<ScanViewConfigFolder>()
    private val fileList = mutableListOf<ScanViewConfigFile>()

    private val allFiles: List<ScanViewConfigFile>
        get() {
            return fileList + folderList.flatMap { it.allFiles }
        }

    enum class ListOptions(val description: String) {
        OriginalStructure("Original folder structure"),
        GroupByPluginType("Group by plugin type"),
        GroupByCategory("Group by category"),
        GroupByWorkflow("Group by workflow");

        companion object {
            fun asDescriptionList(): Array<String?> {
                val descriptionList = values().map { it.description }
                return descriptionList.toTypedArray()
            }
        }
    }

    fun asOrderedList(): List<Any> {
        return mutableListOf<Any>().apply {
            addAll(folderList)
            addAll(fileList)
        }
    }

    companion object {
        fun loadFromFolder(context: Context,
                          assetFolderName: String,
                          friendlyName: String,
                          listOptions: ListOptions): ScanViewConfigFolder? {

            val scanViewConfigOriginalFolder = loadRecursivelyFromFolder(context,
                assetFolderName,
                friendlyName,
                null)

            return when (listOptions) {
                ListOptions.OriginalStructure -> {
                    scanViewConfigOriginalFolder
                }
                ListOptions.GroupByPluginType -> {
                    ScanViewConfigFolder(assetFolderName, friendlyName, null).run {
                        scanViewConfigOriginalFolder?.allFiles?.forEach { file ->
                            file.pluginTypeList.forEach { pluginType ->
                                val folder = this.folderList.firstOrNull {
                                    it.friendlyName == pluginType.description
                                } ?: ScanViewConfigFolder(file.assetFolderName, pluginType.description, this).also {
                                    folderList.add(it)
                                }
                                folder.fileList.add(file)
                            }
                        }
                        this
                    }
                }
                ListOptions.GroupByCategory -> {
                    ScanViewConfigFolder(assetFolderName, friendlyName, null).run {
                        scanViewConfigOriginalFolder?.allFiles?.forEach { file ->
                            file.pluginTypeList.forEach { pluginType ->
                                pluginType.categories.forEach { pluginCategory ->
                                    val folder = this.folderList.firstOrNull {
                                        it.friendlyName == pluginCategory.description
                                    } ?: ScanViewConfigFolder(file.assetFolderName, pluginCategory.description, this).also {
                                        folderList.add(it)
                                    }
                                    folder.fileList.add(file)
                                }
                            }
                        }
                        this
                    }
                }
                ListOptions.GroupByWorkflow -> {
                    ScanViewConfigFolder(assetFolderName, friendlyName, null).run {
                        scanViewConfigOriginalFolder?.allFiles?.forEach { file ->
                            val folder = this.folderList.firstOrNull {
                                it.friendlyName == file.pluginWorkflow.description
                            } ?: ScanViewConfigFolder(file.assetFolderName, file.pluginWorkflow.description, this).also {
                                folderList.add(it)
                            }
                            folder.fileList.add(file)
                        }
                        this
                    }
                }
            }
        }

        private fun loadRecursivelyFromFolder(context: Context,
                           assetFolderName: String,
                           friendlyName: String,
                           parent: ScanViewConfigFolder?): ScanViewConfigFolder? {
            val scanViewConfigFolder = ScanViewConfigFolder(assetFolderName, friendlyName, parent)
            context.assets.list(assetFolderName)?.also { listFiles ->
                if (listFiles.isNotEmpty()) {
                    listFiles.forEach { fileOrFolder ->
                        try {
                            val subFolder = loadRecursivelyFromFolder(context,
                                "$assetFolderName${File.separatorChar}$fileOrFolder",
                                fileOrFolder,
                                scanViewConfigFolder)
                            subFolder?.let {
                                scanViewConfigFolder.folderList.add(it)
                            }
                        }
                        catch (e: IOException) {
                            return null
                        }
                    }
                }
                else {
                    parent?.let { parentFolder ->
                        parentFolder.fileList.add(
                            ScanViewConfigFile.loadFromFile(context, parentFolder.assetFolderName, friendlyName))
                    }
                    return null
                }
            }
            return scanViewConfigFolder
        }
    }
}
