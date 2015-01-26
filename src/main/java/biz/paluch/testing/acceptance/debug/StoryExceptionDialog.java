package biz.paluch.testing.acceptance.debug;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Layout;
import org.openqa.selenium.WebDriver;

import biz.paluch.testing.StackTraceFilter;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

public class StoryExceptionDialog extends JDialog {

    private static final String UNDO_ACTION = "undo";
    private static final String REDO_ACTION = "redo";
    private static int lastX = -1;
    private static int lastY = -1;

    private static int lastWidth = 800;
    private static int lastHeight = 600;

    private JPanel contentPane;
    private JButton buttonRetry;
    private JButton buttonCancel;
    private JTextPane exceptionPanel;
    private JButton buttonContinue;
    private JButton buttonEvaluate;
    private JTextField debugExpression;

    private boolean doContinue;
    private boolean doCancel;
    private boolean doRetry;
    private WebDriver webDriver;

    public StoryExceptionDialog() {
        super();
    }

    private void initialize() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonContinue);
        buttonRetry.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onRetry();
            }
        });

        buttonContinue.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (debugExpression.hasFocus()) {
                    onEvaluate(debugExpression.getText());
                    return;
                }
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonEvaluate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onEvaluate(debugExpression.getText());
            }
        });

        exceptionPanel.getMargin().set(5, 5, 5, 5);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        final UndoManager undoMgr = new UndoManager();

        debugExpression.setDocument(new PlainDocument());

        // Add listener for undoable events
        debugExpression.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent pEvt) {
                undoMgr.addEdit(pEvt.getEdit());
            }
        });

        // Add undo/redo actions
        debugExpression.getActionMap().put(UNDO_ACTION, new AbstractAction(UNDO_ACTION) {
            public void actionPerformed(ActionEvent pEvt) {
                try {
                    if (undoMgr.canUndo()) {
                        undoMgr.undo();
                    }
                } catch (CannotUndoException e) {
                    e.printStackTrace();
                }
            }
        });
        debugExpression.getActionMap().put(REDO_ACTION, new AbstractAction(REDO_ACTION) {
            public void actionPerformed(ActionEvent pEvt) {
                try {
                    if (undoMgr.canRedo()) {
                        undoMgr.redo();
                    }
                } catch (CannotRedoException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create keyboard accelerators for undo/redo actions (Ctrl+Z/Ctrl+Y)
        debugExpression.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), UNDO_ACTION);
        debugExpression.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), REDO_ACTION);
        debugExpression.setText("html();");

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        doContinue = true;
        dispose();
    }

    private void onCancel() {
        doCancel = true;
        dispose();
    }

    private void onRetry() {
        doRetry = true;
        dispose();
    }

    private void onEvaluate(String expression) {

        if (StringUtils.isBlank(expression)) {
            setContent("");
            return;
        }

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");

        // because of threading!
        WebDriverRunner.setWebDriver(webDriver);
        engine.put("selenide", new Selenide());
        engine.put("wd", webDriver);

        String code = "function $(args){ return selenide.$(args);} function $$(args){ return selenide.$$(args);} function html() {return wd.getPageSource()} "
                + expression;
        try {
            String result = "" + engine.eval(code);
            setContent(result);
        } catch (ScriptException e) {
            exceptionOccured(expression, e);
        }
    }

    public static StoryExceptionDialog open(String label, Throwable exception, WebDriver webDriver) {
        StoryExceptionDialog dialog = new StoryExceptionDialog();
        dialog.initialize();
        dialog.webDriver = webDriver;
        dialog.exceptionOccured(label, exception);

        Dimension dialogSize = new Dimension(lastWidth, lastHeight);
        if (lastX != -1 && lastY != -1) {
            dialog.setLocation(lastX, lastY);
        } else {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int halfScreenHeight = screenSize.height / 2;
            int halfScreenWidth = screenSize.width / 2;

            int halfWidth = dialogSize.height / 2;
            int halfHeight = dialogSize.width / 2;
            Point dialogPosition = new Point(halfScreenWidth - halfWidth, halfScreenHeight - halfHeight);

            dialog.setLocation(dialogPosition);
        }
        dialog.setSize(dialogSize);
        dialog.doLayout();
        dialog.setVisible(true);
        dialog.requestFocus();

        lastX = dialog.getLocation().x;
        lastY = dialog.getLocation().y;

        lastWidth = (int) dialog.getSize().getWidth();
        lastHeight = (int) dialog.getSize().getHeight();

        return dialog;
    }

    public void exceptionOccured(String label, Throwable exception) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(label).append(Layout.LINE_SEP);
        buffer.append(StackTraceFilter.getFilteredStackTrace(exception));
        setContent(buffer);
    }

    private void setContent(CharSequence buffer) {
        exceptionPanel.setText(buffer.toString());
        exceptionPanel.setEditable(false);
    }

    public boolean isDoContinue() {
        return doContinue;
    }

    public boolean isDoCancel() {
        return doCancel;
    }

    public boolean isDoRetry() {
        return doRetry;
    }
}
