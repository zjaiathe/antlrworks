/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package org.antlr.works.menu;

import edu.usfca.xj.appkit.frame.XJDialog;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.works.components.grammar.CEditorGrammar;
import org.antlr.works.generate.CodeDisplay;
import org.antlr.works.generate.CodeGenerate;
import org.antlr.works.generate.CodeGenerateDelegate;
import org.antlr.works.generate.DialogGenerate;
import org.antlr.works.stats.StatisticsAW;

public class MenuGenerate extends MenuAbstract implements CodeGenerateDelegate {

    public CodeGenerate generateCode = null;

    protected String actionShowCodeRule;
    protected boolean actionShowCodeLexer;
    protected boolean actionShowCodeAfterGeneration = false;

    public MenuGenerate(CEditorGrammar editor) {
        super(editor);
        generateCode = new CodeGenerate(editor, this);
    }

    public void generateCode() {
        actionShowCodeRule = null;
        generateCodeProcess();
    }

    protected void generateCodeProcess() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_GENERATE_CODE);

        if(!editor.ensureDocumentSaved())
            return;

        if(!checkLanguage())
            return;

        DialogGenerate dialog = new DialogGenerate(editor.getWindowContainer());
        if(dialog.runModal() == XJDialog.BUTTON_OK) {
            editor.getDocument().performAutoSave();

            generateCode.setDebug(dialog.generateDebugInformation());
            generateCode.setOutputPath(dialog.getOutputPath());
            generateCode.generateInThread(editor.getJavaContainer());
        }
    }

    public boolean checkLanguage() {
        if(!isKnownLanguage()) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Can only generate grammar for Java language");
            return false;
        } else
            return true;
    }

    public boolean isKnownLanguage() {
        String language = generateCode.getGrammarLanguage();
        return language != null && language.equals("Java");
    }

    public void showGeneratedCode(boolean lexer) {
        StatisticsAW.shared().recordEvent(lexer?StatisticsAW.EVENT_SHOW_LEXER_GENERATED_CODE:StatisticsAW.EVENT_SHOW_PARSER_GENERATED_CODE);

        if(lexer && !generateCode.supportsLexer()) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the lexer because there is no lexer in this grammar.");
            return;
        } else if(!lexer && !generateCode.supportsParser()) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the parser because there is no parser in this grammar.");
            return;
        }

        showGeneratedCode(null, lexer);
    }

    public void showRuleGeneratedCode() {
        StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_SHOW_RULE_GENERATED_CODE);

        if(editor.getCurrentRule() == null)
            XJAlert.display(editor.getWindowContainer(), "Error", "A rule must be selected first.");
        else
            showGeneratedCode(editor.getCurrentRule().name, false);
    }

    public void showGeneratedCode(String rule, boolean lexer) {
        if(!checkLanguage())
            return;

        if(!generateCode.isGeneratedTextFileExisting(lexer)
                || generateCode.isFileModifiedSinceLastGeneration()
                || editor.getDocument().isDirty()) {
            // Generate automatically the code and call again
            // this method (using actionShowCodeRule as flag)
            actionShowCodeRule = rule;
            actionShowCodeLexer = lexer;
            actionShowCodeAfterGeneration = true;
            generateCodeProcess();
            return;
        }

        CodeDisplay dc = new CodeDisplay(editor.getXJFrame());
        String name = generateCode.getGrammarName();
        if(lexer)
            name += "Lexer";

        String title = name+".java";
        String text = generateCode.getGeneratedText(lexer);
        if(rule != null) {
            int startIndex = text.indexOf("$ANTLR start "+rule);
            startIndex = text.indexOf("\n", startIndex)+1;
            int stopIndex = text.indexOf("$ANTLR end "+rule);
            while(stopIndex>0 && text.charAt(stopIndex) != '\n')
                stopIndex--;

            if(startIndex >= 0 && stopIndex >= 0) {
                text = text.substring(startIndex, stopIndex);
                title = rule;
            } else {
                XJAlert.display(editor.getWindowContainer(), "Error", "Cannot find markers for rule \""+rule+"\"");
                return;
            }
        }
        dc.setText(text);
        dc.setTitle(title);

        editor.addTab(dc);
        editor.makeBottomComponentVisible();
    }

    public boolean codeGenerateDisplaySuccess() {
        return !actionShowCodeAfterGeneration;
    }

    public void codeGenerateDidComplete() {
        if(actionShowCodeAfterGeneration) {
            actionShowCodeAfterGeneration = false;
            showGeneratedCode(actionShowCodeRule, actionShowCodeLexer);
        }
    }

}
