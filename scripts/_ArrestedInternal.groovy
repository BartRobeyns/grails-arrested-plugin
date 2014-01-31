installTemplate = { String artefactName, String artefactPath, String templatePath ->
    installTemplateEx(artefactName, artefactPath, templatePath, artefactName, null)
}

installTemplateEx = { String artefactName, String artefactPath, String templatePath, String templateName, Closure c ->
    // Copy over the standard auth controller.
    def artefactFile = "${basedir}/${artefactPath}/${artefactName}"

    if (new File(artefactFile).exists()) {
        println("no existe")
        ant.input(
                addProperty: "${args}.${artefactName}.overwrite",
                message: "${artefactName} already exists. Overwrite? [y/n]")

        if (ant.antProject.properties."${args}.${artefactName}.overwrite" == "n") {
            return
        }
    }

    // Copy the template file to the 'grails-app/controllers' directory.
    templateFile = "${arrestedPluginDir}/src/templates/${templatePath}/${templateName}"
    println(templateFile)
    if (!new File(templateFile).exists()) {
        ant.echo("[Arrested plugin] Error: src/templates/${templatePath}/${templateName} does not exist!")
        return
    }

    ant.copy(file: templateFile, tofile: artefactFile, overwrite: true)

    // Perform any custom processing that may be required.
    if (c) {
        c.delegate = [ artefactFile: artefactFile ]
        c.call()
    }
    println(artefactFile)
    println(templateFile)
    event("CreatedFile", [artefactFile])
}

target(createController: "Creates a standard controller") {
    def (pkg, prefix) = parsePrefix()
    // Copy over the standard filters class.
    def className = "Arrested"+prefix
    installTemplateEx("${className}.groovy", "grails-app/controllers${packageToPath(pkg)}", "controllers", "Controller.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
            ant.replacefilter(token: '@controller.name@', value: className)
            ant.replacefilter(token: '@class.name@', value: prefix.toUpperCase())
            ant.replacefilter(token: '@class.instance@', value: prefix)
        }
    }
}

target(createToken: "Create a token class") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("ArrestedToken.groovy", "grails-app/domain${packageToPath(pkg)}", "classes", "ArrestedToken.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

target(createUser: "Create a user class") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("ArrestedUser.groovy", "grails-app/domain${packageToPath(pkg)}", "classes", "ArrestedUser.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

target(createUserController: "Create a user class") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("ArrestedUserController.groovy", "grails-app/controllers${packageToPath(pkg)}", "controllers", "ArrestedUserController.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

target(createAuth: "Create a authentication controller") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("AuthController.groovy", "grails-app/controllers${packageToPath(pkg)}", "controllers", "AuthController.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

target(createFilter: "Create a security filter") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("SecurityFilters.groovy", "grails-app/conf${packageToPath(pkg)}", "configuration", "SecurityFilters.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

target(updateUrl: "Updating the Url Mappings") {
    def (pkg, prefix) = parsePrefix()
    installTemplateEx("UrlMappings.groovy", "grails-app/conf${packageToPath(pkg)}", "configuration", "UrlMappings.groovy") {
        ant.replace(file: artefactFile) {
            ant.replacefilter(token: "@package.line@", value: (pkg ? "package ${pkg}\n\n" : ""))
        }
    }
}

private parsePrefix() {
    def prefix = "Arrested"
    def pkg = ""
    if (argsMap["name"] != null) {
        def givenValue = argsMap["name"].split(/\./, -1)
        prefix = givenValue[-1]
        pkg = givenValue.size() > 1 ? givenValue[0..-2].join('.') : ""
    }
    return [ pkg, prefix ]
}

private packageToPath(String pkg) {
    return pkg ? '/' + pkg.replace('.' as char, '/' as char) : ''
}