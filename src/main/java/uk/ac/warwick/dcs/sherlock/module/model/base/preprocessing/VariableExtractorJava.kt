package uk.ac.warwick.dcs.sherlock.module.model.base.preprocessing

import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import uk.ac.warwick.dcs.sherlock.api.model.preprocessing.IAdvancedPreProcessor
import uk.ac.warwick.dcs.sherlock.api.util.IndexedString
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaLexer
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParser
import uk.ac.warwick.dcs.sherlock.module.model.base.lang.JavaParserBaseListener

internal class VariableExtractorJava : IAdvancedPreProcessor<JavaLexer> {
    override fun process(lexer: JavaLexer): List<IndexedString> {
        val fields = mutableListOf<IndexedString>()

        lexer.reset()
        val parser = JavaParser(CommonTokenStream(lexer))

        ParseTreeWalker.DEFAULT.walk(object : JavaParserBaseListener() {
            //globals
            override fun enterFieldDeclaration(ctx: JavaParser.FieldDeclarationContext) {
                fields.add(
                    IndexedString(
                        ctx.start.line,
                        ctx.text.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    )
                )
            }

            //locals
            override fun enterLocalVariableDeclaration(ctx: JavaParser.LocalVariableDeclarationContext) {
                fields.add(
                    IndexedString(
                        ctx.start.line,
                        ctx.text.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    )
                )
            }
        }, parser.compilationUnit())

        return fields
    }
}
