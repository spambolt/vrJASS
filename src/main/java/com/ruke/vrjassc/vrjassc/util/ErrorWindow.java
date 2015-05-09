package com.ruke.vrjassc.vrjassc.util;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;

public class ErrorWindow extends JFrame implements Runnable {

	private String message;
	private String code;
	private int line;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
     * Creates new form NewJFrame
     */
    public ErrorWindow(String message, String code, int line) {
    	this.message = message;
    	this.code = code;
    	this.line = line;

        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ErrorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ErrorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ErrorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ErrorWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(this);
    }

    public void run() {
    	this.initComponents();
    	this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jStatusBar = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("vrJASS");
        setAlwaysOnTop(true);
        setPreferredSize(new java.awt.Dimension(700, 350));

        jButton1.setText("Close");
        jButton1.setName(""); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setTabSize(4);
        jTextArea1.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				jTextArea1CaretUpdate(e);
			}
		});
        jTextArea1.setText(this.code);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);

		int line = Math.min(this.line, jTextArea1.getLineCount());
		int startOfLineOffset = 0;

		try {
			startOfLineOffset = jTextArea1.getLineStartOffset(line)-1;
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		jTextArea1.setCaretPosition(startOfLineOffset);

        jScrollPane1.setViewportView(jTextArea1);

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setText(this.message);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    private void jTextArea1CaretUpdate(CaretEvent e) {
    	int linenum = 1;
        int columnnum = 1;

        // We create a try catch to catch any exceptions. We will simply ignore such an error for our demonstration.
        try {
            // First we find the position of the caret. This is the number of where the caret is in relation to the start of the JTextArea
            // in the upper left corner. We use this position to find offset values (eg what line we are on for the given position as well as
            // what position that line starts on.
            int caretpos = this.jTextArea1.getCaretPosition();
            linenum = this.jTextArea1.getLineOfOffset(caretpos);

            // We subtract the offset of where our line starts from the overall caret position.
            // So lets say that we are on line 5 and that line starts at caret position 100, if our caret position is currently 106
            // we know that we must be on column 6 of line 5.
            columnnum = caretpos - jTextArea1.getLineStartOffset(linenum);

            // We have to add one here because line numbers start at 0 for getLineOfOffset and we want it to start at 1 for display.
            linenum += 1;
        }
        catch(Exception ex) { }

    	this.jStatusBar.setText(String.valueOf(linenum) + ":" + String.valueOf(columnnum));
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jStatusBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration

}
