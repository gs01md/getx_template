import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import helper.DataService
import helper.GetXName
import helper.TemplateInfo
import view.GetXListener
import view.GetXTemplateView
import java.io.*
import java.util.*


class NewGetX : AnAction() {
    private var project: Project? = null
    private lateinit var psiPath: String
    private var data = DataService.instance

    /**
     * module name
     */
    private lateinit var moduleName: String


    override fun actionPerformed(event: AnActionEvent) {
        project = event.project
        psiPath = event.getData(PlatformDataKeys.PSI_ELEMENT).toString()
        psiPath = psiPath.substring(psiPath.indexOf(":") + 1)
        initView()
    }

    private fun initView() {
        GetXTemplateView(object : GetXListener {
            override fun onSave(): Boolean {
                return save()
            }

            override fun onDataChange(view: GetXTemplateView) {
                //module name
                moduleName = view.nameTextField.text

                //deal default value
                val modelType = view.modeGroup.selection.actionCommand
                data.modeDefault = (GetXName.ModeDefault == modelType)
                data.modeEasy = (GetXName.ModeEasy == modelType)

                //function area
                data.function.useFolder = view.folderBox.isSelected
                data.function.usePrefix = view.prefixBox.isSelected
                data.function.isPageView = view.pageViewBox.isSelected
                data.function.autoDispose = view.disposeBox.isSelected
                data.function.addLifecycle = view.lifecycleBox.isSelected
                data.function.addBinding = view.bindingBox.isSelected
                data.function.lintNorm = view.lintNormBox.isSelected
                val templateType = view.templateGroup.selection.actionCommand
                val list = ArrayList<TemplateInfo>().apply {
                    add(data.templatePage.apply { selected = (GetXName.templatePage == templateType) })
                    add(data.templateComponent.apply { selected = (GetXName.templateComponent == templateType) })
                    add(data.templateCustom.apply { selected = (GetXName.templateCustom == templateType) })
                }
                for (item in list) {
                    if (!item.selected) continue
                    data.module.logicName = item.logic
                    data.module.stateName = item.state
                    data.module.viewName = item.view
                    data.module.viewFileName = item.viewFile
                    break
                }
            }
        })
    }

    /**
     * generate  file
     */
    private fun save(): Boolean {
        if ("" == moduleName.trim { it <= ' ' }) {
            Messages.showInfoMessage(project, "Please input the module name", "Info")
            return false
        }
        //Create a file
        createFile()
        //Refresh project
        project?.guessProjectDir()?.refresh(false, true)

        return true
    }

    private fun createFile() {
        val prefix = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, upperCase(moduleName))
        var folder = ""
        var prefixName = ""

        //add folder
        if (data.function.useFolder) {
            folder = "/$prefix"

            //use folder suffix
            if (data.setting.useFolderSuffix) {
                folder = "${folder}_${data.module.viewName.lowercase()}"
            }
        }

        //add prefix
        if (data.function.usePrefix) {
            prefixName = "${prefix}_"
        }

        //select generate file mode
        val path = psiPath + folder
        if (data.modeDefault) {
            generateDefault(path, prefixName)
        } else if (data.modeEasy) {
            generateEasy(path, prefixName)
        }

        //add binding file
        if (data.function.addBinding) {
            generateFile("binding.dart", path, "${prefixName}binding.dart")
        }
    }

    private fun generateDefault(path: String, prefixName: String) {

//        generateFile("binding.dart", path, "${prefixName}binding.dart")
        generateFile("logic.dart", path, "$prefixName${data.module.logicName.lowercase(Locale.getDefault())}.dart")
        generateFile("view.dart", path, "$prefixName${data.module.viewFileName.lowercase(Locale.getDefault())}.dart")
        generateFile("entity.dart", path, "${prefixName}entity.dart")
        generateFile("provider.dart", path, "$prefixName${data.module.providerName.lowercase(Locale.getDefault())}.dart")
        generateFile("repository.dart", path, "$prefixName${data.module.repositoryName.lowercase(Locale.getDefault())}.dart")
    }

    private fun generateEasy(path: String, prefixName: String) {
        generateFile(
            "easy/logic.dart",
            path,
            "${prefixName}${data.module.logicName.lowercase(Locale.getDefault())}.dart"
        )
        generateFile(
            "easy/view.dart",
            path,
            "${prefixName}${data.module.viewFileName.lowercase(Locale.getDefault())}.dart"
        )
    }

    private fun generateFile(inputFileName: String, filePath: String, outFileName: String) {
        //content deal
        val content = dealContent(inputFileName)

        //Write file
        try {
            val folder = File(filePath)
            // if file not exists, then create it
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val file = File("$filePath/$outFileName")
            if (!file.exists()) {
                file.createNewFile()
            }
            val fw = FileWriter(file.absoluteFile)
            val bw = BufferedWriter(fw)
            bw.write(content)
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //content need deal
    private fun dealContent(inputFileName: String): String {
        //module name
        val name = upperCase(moduleName)
        //Adding a prefix requires modifying the imported class name
        var prefixName = ""
        if (data.function.usePrefix) {
            prefixName = "${CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)}_"
        }

        //select suitable file, return suitable content
        var content = getSuitableContent(inputFileName)

        //replace view file
        content = replaceView(content, inputFileName, prefixName)

        //replace logic file
        content = replaceLogic(content, inputFileName, prefixName)

        //replace binding file
        content = replaceBinding(content, inputFileName, prefixName)

        //replace logic file
        content = replaceProvider(content, inputFileName, prefixName)

        //replace binding file
        content = replaceRepository(content, inputFileName, prefixName)

        //replace state file
        content = replaceState(content, inputFileName,prefixName)

        content = content.replace("@name".toRegex(), name)

        return content
    }


    private fun replaceProvider(content: String, inputFileName: String, prefixName: String): String {
        var tempContent = content
        if (!inputFileName.contains("provider.dart")) {
            return tempContent
        }

//        tempContent = tempContent.replace(
//            "entity.dart".toRegex(),
//            "$prefixName${data.module.stateName.lowercase(Locale.getDefault())}.dart"
//        )

//        tempContent = tempContent.replace("@nameInterfaceProvider".toRegex(), "${data.module.providerInterfaceName}")
//        tempContent = tempContent.replace("@nameProvider".toRegex(), "${data.module.providerName}")
        tempContent = tempContent.replace("State", "Entity")
        tempContent = tempContent.replace("state", "entity")


        return tempContent
    }

    private fun replaceRepository(content: String, inputFileName: String, prefixName: String): String {
        var tempContent = content
        if (!inputFileName.contains("repository.dart")) {
            return tempContent
        }

//        tempContent = tempContent.replace(
//            "entity.dart".toRegex(),
//            "$prefixName${data.module.stateName.lowercase(Locale.getDefault())}.dart"
//        )
//
//        tempContent = tempContent.replace("@nameInterfaceRepository".toRegex(), "${data.module.repositoryInterfaceName}")
//        tempContent = tempContent.replace("@nameRepository".toRegex(), "${data.module.repositoryName}")
//        tempContent = tempContent.replace("State".toRegex(), "${data.module.stateName}")
//        tempContent = tempContent.replace("state".toRegex(), data.module.stateName.lowercase(Locale.getDefault()))

        tempContent = tempContent.replace("State", "Entity")
        tempContent = tempContent.replace("state", "entity")

        return tempContent
    }

    private fun replaceLogic(content: String, inputFileName: String, prefixName: String): String {
        var tempContent = content

        if (!inputFileName.contains("logic.dart")) {
            return tempContent
        }

        tempContent = tempContent.replace("Logic".toRegex(), data.module.logicName)
        tempContent = tempContent.replace("State", "Entity")
        tempContent = tempContent.replace("state", "entity")

        return tempContent
    }

    private fun replaceView(content: String, inputFileName: String, prefixName: String): String {
        var tempContent = content
        if (!inputFileName.contains("view.dart")) {
            return tempContent
        }

        tempContent = tempContent.replace("State", "Entity")
        tempContent = tempContent.replace("state", "entity")

        //deal binding function
        if (data.function.addBinding) {
            tempContent = tempContent.replace("Get.put\\(@nameLogic\\(\\)\\)".toRegex(), "Get.find<@nameLogic>()")
        }

        //remove lint
        if (!data.function.lintNorm || (!data.setting.lint && data.function.lintNorm)) {
            tempContent = tempContent.replace("final @nameLogic".toRegex(), "final")
            tempContent = tempContent.replace("final @nameState".toRegex(), "final")
        }
        //remove flutter_lints
        if (!data.function.lintNorm || (!data.setting.flutterLints && data.function.lintNorm)) {
            tempContent = tempContent.replace(
                "const @namePage\\(\\{Key\\? key}\\) : super\\(key: key\\);\\s*\n\\s\\s".toRegex(),
                ""
            )
            tempContent = tempContent.replace(
                "@namePage\\(\\{Key\\? key}\\) : super\\(key: key\\);\\s*\n\\s\\s".toRegex(),
                ""
            )
        }

        //deal suffix of custom module name
        tempContent = tempContent.replace(
            "logic.dart".toRegex(),
            "$prefixName${data.module.logicName.lowercase(Locale.getDefault())}.dart"
        )
//        tempContent = tempContent.replace(
//            "entity.dart".toRegex(),
//            "$prefixName${data.module.stateName.lowercase(Locale.getDefault())}.dart"
//        )
        tempContent = tempContent.replace("Page".toRegex(), data.module.viewName)
        tempContent = tempContent.replace("Logic".toRegex(), data.module.logicName)
        tempContent = tempContent.replace("logic".toRegex(), data.module.logicName.lowercase(Locale.getDefault()))


        return tempContent
    }

    private fun replaceState(content: String, inputFileName: String, prefixName: String ): String {
        var tempContent = content
        if (!inputFileName.contains("entity.dart")) {
            return tempContent
        }

        tempContent = tempContent.replace("State", "Entity")


        return tempContent
    }

    private fun replaceBinding(content: String, inputFileName: String, prefixName: String): String {
        var tempContent = content
        if (!inputFileName.contains("binding.dart")) {
            return tempContent
        }

        if (data.function.addBinding) {
            tempContent = tempContent.replace("Logic".toRegex(), data.module.logicName)
            tempContent = tempContent.replace(
                "logic.dart".toRegex(),
                "$prefixName${data.module.logicName.lowercase(Locale.getDefault())}.dart"
            )
        }

//        tempContent = tempContent.replace("@nameInterfaceRepository".toRegex(), "${data.module.repositoryInterfaceName}")
//        tempContent = tempContent.replace("@nameRepository".toRegex(), "${data.module.repositoryName}")
//        tempContent = tempContent.replace("@nameState".toRegex(), "${data.module.stateName}")
//        tempContent = tempContent.replace("@nameInterfaceProvider".toRegex(), "${data.module.providerInterfaceName}")
//        tempContent = tempContent.replace("@nameProvider".toRegex(), "${data.module.providerName}")

        return tempContent
    }

    private fun getSuitableContent(inputFileName: String): String {
        //deal auto dispose or pageView
        var defaultFolder = "/templates/normal/"

        // view.dart
        if (inputFileName.contains("view.dart")) {
            if (data.function.autoDispose) {
                defaultFolder = "/templates/auto/"
            }

            if (data.function.isPageView) {
                defaultFolder = "/templates/pageView/"
            }
        }

        //add lifecycle
        if (data.function.addLifecycle && inputFileName.contains("logic.dart")) {
            defaultFolder = "/templates/lifecycle/"
        }

        //read file
        var content = ""
        try {
            val input = this.javaClass.getResourceAsStream("$defaultFolder$inputFileName")
            content = String(readStream(input!!))
        } catch (e: Exception) {
            //some error
        }

        return content
    }

    @Throws(Exception::class)
    private fun readStream(inStream: InputStream): ByteArray {
        val outSteam = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(1024)
            var len: Int
            while (inStream.read(buffer).apply { len = this } != -1) {
                outSteam.write(buffer, 0, len)
                println(String(buffer))
            }
        } catch (_: IOException) {
        } finally {
            outSteam.close()
            inStream.close()
        }
        return outSteam.toByteArray()
    }

    private fun upperCase(str: String): String {
        return str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
    }
}
