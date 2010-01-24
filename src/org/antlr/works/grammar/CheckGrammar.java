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

package org.antlr.works.grammar;

import org.antlr.works.components.editor.GrammarEditor;
import org.antlr.works.grammar.antlr.GrammarResult;
import org.antlr.works.utils.Console;

public class CheckGrammar implements Runnable {

    private volatile CheckGrammarDelegate delegate;
    private volatile GrammarEditor editor;

    private volatile boolean cancelled = false;

    public CheckGrammar(GrammarEditor editor, CheckGrammarDelegate delegate) {
        this.editor = editor;
        this.delegate = delegate;
    }

    public void close() {
        editor.hideProgress();                
        editor = null;
        delegate = null;
    }

    public void check() {
        new Thread(this).start();
    }

    public void cancel() {
        cancelled = true;
        editor.getGrammarEngine().cancelAnalyze();
    }

    public void run() {
        editor.getConsole().setMode(Console.MODE_VERBOSE);
        delegate.checkGrammarDidBegin(this);
        GrammarResult result;
        try {
            result = editor.getGrammarEngine().analyze();
        } catch (Exception e) {
            editor.getConsole().println(e);
            // Result cannot be null, so report the exception
            result = new GrammarResult(e);
        }
        if(!cancelled) {
            delegate.checkGrammarDidEnd(this, result);            
        }
    }

}
