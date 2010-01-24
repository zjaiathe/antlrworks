package org.antlr.works.grammar.decisiondfa;

import org.antlr.works.components.editor.GrammarEditor;
import org.antlr.works.editor.EditorInspectorItem;
import org.antlr.works.editor.idea.IdeaAction;

import java.util.ArrayList;
import java.util.List;

public class DecisionDFAItem extends EditorInspectorItem {

    private GrammarEditor editor;

    public DecisionDFAItem(GrammarEditor editor) {
        this.editor = editor;
    }

    @Override
    public List<IdeaAction> getIdeaActions() {
        List<IdeaAction> actions = new ArrayList<IdeaAction>();
        actions.add(new IdeaAction("Show Decision DFA", this, IDEA_DECISION_DFA, token));
        return actions;
    }

    @Override
    public void ideaActionFire(IdeaAction action, int actionID) {
        switch(actionID) {
            case IDEA_DECISION_DFA:
                DecisionDFA decision = new DecisionDFA(editor);
                decision.launch();
                break;
        }
    }

}
