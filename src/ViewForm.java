
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ViewForm.java
 *
 * Created on 01-may-2010, 11:53:56
 */

/**
 *
 * @author Administrador
 */



public class ViewForm extends javax.swing.JFrame {
    /** Creates new form ViewForm */
    Graphics graphics;
    Image image;
    uCAM cam;
    
    ChangeListener clDiv = new ChangeListener() {

        public void stateChanged(ChangeEvent e) {
            long baud_rate;
            int  div1;
            int  div2;

            
            div1 = Integer.parseInt(sDiv1.getValue().toString());
            div2 = Integer.parseInt(sDiv2.getValue().toString());
            baud_rate = 14745600/(4*(div2 + 1)*(div1 + 1));
            lblBaud.setText(String.valueOf(baud_rate));
        }
    };

    public ViewForm() {
        initComponents();
    }

    public void initTabs() {
        comTabInit();
        textAreaClear();
        textAreaInit();
        picTabInit();
        specialTabInit();
    }
    public void comTabInit() {
        int i;
        long []speed = {  110,    300,    600,   1200,
                         2400,   4800,   9600,  14400,
                        19200,  28800,  38400,  56000,
                        57600, 115200, 128000, 256000};


        for (i = 0; i < speed.length; i++) {  // add default speeds
            cbSpeed.addItem(speed[i]);
        }

        for(i = 1; i < 256; i++) {  // add all com ports
            cbPort.addItem(i);
        }

        bCommClose.setEnabled(false);
        lpBaud.setVisible(false);  // init the comm tab
        cbPort.setSelectedIndex(2);
        cbSpeed.setSelectedIndex(13);
        sDiv1.setValue(3);
        sDiv2.setValue(7);
        sDiv1.addChangeListener(clDiv);  // add custom listeners (events)
        sDiv2.addChangeListener(clDiv);
    }
    public void picTabInit() {
        lblJPEG.setVisible(false);
        cbJPEG.setVisible(false);
        lblPacket.setVisible(false);
        sPacket.setVisible(false);
        rbRAW.setVisible(false);
        rbJPEG.setVisible(false);
    }
    public void textAreaClear() {
        taLog.setText("");
    }
    public void textAreaInit() {
        portSelected(null);
        speedSelected(null);
        fullRstSelEv(null);
        specRstSelEv(null);
    }
    public void specialTabInit() {
        lightSelected(null);
    }
    
    public void paint(short[] arr) {
        int x;
        int y;
        short rgb;
        short temp;
        short red;
        short green;
        short blue;
        byte gray;
        int ix;
        int w;
        int h;
        String dim;
        String []dimArr;


        ix = 0;
        dim = cbRAW.getSelectedItem().toString();
        dimArr = dim.split("x");
        w = Integer.parseInt(dimArr[0].trim());
        h = Integer.parseInt(dimArr[1].trim());
        graphics = this.cimg.getGraphics();
        graphics.setColor(new Color(236, 236, 236));
        graphics.drawRect(0, 0, 640, 480);
        graphics.fillRect(0, 0, 640, 480);
        cam.flush();
        for(y = 0; y < h; y++) {
            for(x = 0; x < w; x++) {
                rgb  = arr[ix++];
                if(cbColType.getSelectedIndex() < 6) {
                    switch(getBits()) {
                        case 2:
                            gray = (byte) (rgb & 0xC0);
                            graphics.setColor(new Color(gray & 0xC0, gray & 0xC0, gray & 0xC0));
                            graphics.drawRect(x, y, 4, 4);
                            x += 3;
                            if (x >= w) {
                                y += 3;
                            }
                            break;
                        case 4:
                            gray = (byte) (rgb & 0xF0);
                            graphics.setColor(new Color(gray & 0xF0, gray & 0xF0, gray & 0xF0));
                            graphics.drawRect(x, y, 2, 2);
                            x++;
                            if (x >= w) {
                                y++;
                            }
                            break;
                        case 8:
                            if(cbColType.getSelectedIndex() == 3) {
                                red   = (short)(((rgb >> 5) & 0x07) << 5);
                                green = (short)(((rgb >> 2) & 0x07) << 5);
                                blue  = (short)(( rgb       & 0x03) << 6);
                                graphics.setColor(new Color(red & 0xFF, green & 0xFF, blue & 0xFF));
                            } else {
                                gray = (byte) (rgb);
                                graphics.setColor(new Color(gray & 0xFF, gray & 0xFF, gray & 0xFF));
                            }
                            graphics.drawRect(x, y, 1, 1);
                            break;
                        case 12:
                            red = 0;
                            green = 0;
                            blue = 0;
                            graphics.setColor(new Color(red & 0xFF, green & 0xFF, blue & 0xFF));
                            graphics.drawRect(x, y, 1, 1);
                            break;
                        case 16:
                            temp  = arr[ix++];
                            red   = (short)(rgb & 0xF8);
                            green = (short)(((rgb & 0x07) << 5) | ((temp & 0xE0) >> 3));
                            blue  = (short)((temp & 0x1F) << 3);
                            graphics.setColor(new Color(red & 0xFF, green & 0xFF, blue & 0xFF));
                            graphics.drawRect(x, y, 1, 1);
                            break;
                        default:
                            // JPEG
                            break;
                    }
                }
            }
        }
    }

    public byte getPort() {
        return (byte)(Integer.parseInt(cbPort.getItemAt(cbPort.getSelectedIndex()).toString()));
    }
    public int getBauds() {
        if(rbStd.isSelected() == true) {
            return Integer.parseInt(cbSpeed.getItemAt(cbSpeed.getSelectedIndex()).toString());
        } else {
            return 115200;
            // Custom baud rate code
        }
    }
    public byte getType() {
        if(chkFullRst.isSelected() == true) {
            return 0x00;
        } else {
            return 0x01;
        }
    }
    public byte getSpecial() {
        if(chkSpecRst.isSelected() == true) {
            return (byte)0xFF;
        } else {
            return 0x00;
        }
    }
    public byte getLight() {
        if(rbLight50.isSelected() == true) {
            return 0;
        } else {
            return 1;
        }
    }
    public byte getColType() {
        return (byte) (cbColType.getSelectedIndex() + 1);
    }
    public byte getRAWRes() {
        return (byte)(1 + cbRAW.getSelectedIndex()*2);
    }
    public byte getJPEGRes() {
        return (byte)(1 + cbJPEG.getSelectedIndex()*2);
    }
    public byte[] getInitial() {
        byte[] initial = new byte[3];

        initial[0] = getColType();
        initial[1] = getRAWRes();
        initial[2] = getJPEGRes();
        return initial;
    }
    public short getPkgSize() {
        return (short)(Integer.parseInt(sPacket.getValue().toString()));
    }
    public byte getSnapType() {
        if(cbColType.getSelectedIndex() < 6) {
            return 1;
        } else {
            return 0;
        }
    }
    public short getSkipCtr() {
        return (short)(Integer.parseInt(sSkip.getValue().toString()));
    }
    public short[] getSnapshot() {
        short[] snapshot;
        
        
        snapshot = new short[2];
        snapshot[0] = getSnapType();
        snapshot[1] = getSkipCtr();
        return snapshot;
    }
    public byte getPicType() {
        if(rbSnap.isSelected() == true) {
            return 1;
        } else {
            if (rbPrev.isSelected() == true && rbRAW.isSelected() == true) {
                return 2;
            } else {
                return 5;
            }
        }
    }

    public byte fsmCmd(byte fsmstate) {
        if(rbSnap.isSelected() == true) {
            if(cbColType.getSelectedIndex() < 6 && fsmstate == 3) { // is raw
                return (byte)4;
            }
            if(chkVidMode.isSelected() == true && fsmstate == 7) {
                return (byte)4;
            }
        }
        if(rbPrev.isSelected() == true) {
            if(rbRAW.isSelected() == true && fsmstate == 3) {
                return (byte)5;
            }
            if(chkVidMode.isSelected() == true && fsmstate == 7) {
                return (byte)5;
            }
        }
        return fsmstate;
    }

    public byte getBits() {
        String s;
        String []st;


        s  = cbColType.getItemAt(cbColType.getSelectedIndex()).toString();
        st = s.split("-");
        s = st[0];
        return (byte) Integer.parseInt(s);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        bgRButton = new javax.swing.ButtonGroup();
        bgLightGroup = new javax.swing.ButtonGroup();
        bgSpeed = new javax.swing.ButtonGroup();
        bgJPEGRAW = new javax.swing.ButtonGroup();
        cimg = new java.awt.Canvas();
        jScrollPane1 = new javax.swing.JScrollPane();
        taLog = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        cbPort = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        lblSpeed = new javax.swing.JLabel();
        cbSpeed = new javax.swing.JComboBox();
        lpBaud = new javax.swing.JLayeredPane();
        sDiv2 = new javax.swing.JSpinner();
        sDiv1 = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblBaud = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        rbCstm = new javax.swing.JRadioButton();
        rbStd = new javax.swing.JRadioButton();
        bCommOpen = new javax.swing.JButton();
        bCommClose = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        chkFullRst = new javax.swing.JCheckBox();
        chkSpecRst = new javax.swing.JCheckBox();
        bReset = new javax.swing.JButton();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        rbLight50 = new javax.swing.JRadioButton();
        rbLight60 = new javax.swing.JRadioButton();
        jPanel2 = new javax.swing.JPanel();
        cbColType = new javax.swing.JComboBox();
        cbRAW = new javax.swing.JComboBox();
        cbJPEG = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        lblRAW = new javax.swing.JLabel();
        lblJPEG = new javax.swing.JLabel();
        sSkip = new javax.swing.JSpinner();
        lblSkip = new javax.swing.JLabel();
        lblPacket = new javax.swing.JLabel();
        sPacket = new javax.swing.JSpinner();
        rbRAW = new javax.swing.JRadioButton();
        rbSnap = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        chkVidMode = new javax.swing.JCheckBox();
        rbJPEG = new javax.swing.JRadioButton();
        rbPrev = new javax.swing.JRadioButton();
        bGo = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        pbPic = new javax.swing.JProgressBar();
        jLabel14 = new javax.swing.JLabel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                onCloseWin(evt);
            }
        });

        cimg.setBackground(getForeground());
        cimg.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));

        taLog.setColumns(20);
        taLog.setEditable(false);
        taLog.setRows(5);
        taLog.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jScrollPane1.setViewportView(taLog);

        jTabbedPane1.setBackground(getForeground());
        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTabbedPane1.setName("t1\nt2"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        cbPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portSelected(evt);
            }
        });

        jLabel1.setText("COM PORT");

        lblSpeed.setText("SPEED");

        cbSpeed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speedSelected(evt);
            }
        });

        lpBaud.setBorder(javax.swing.BorderFactory.createTitledBorder("Baud Settings"));

        sDiv2.setModel(new javax.swing.SpinnerNumberModel(1, 0, 255, 1));
        sDiv2.setBounds(180, 20, 40, -1);
        lpBaud.add(sDiv2, javax.swing.JLayeredPane.DEFAULT_LAYER);

        sDiv1.setModel(new javax.swing.SpinnerNumberModel(1, 0, 255, 1));
        sDiv1.setEditor(new javax.swing.JSpinner.NumberEditor(sDiv1, ""));
        sDiv1.setBounds(70, 20, 40, 20);
        lpBaud.add(sDiv1, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel10.setText("Divider 2");
        jLabel10.setBounds(120, 20, 50, 20);
        lpBaud.add(jLabel10, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel11.setText("Divider 1");
        jLabel11.setBounds(10, 20, 50, 20);
        lpBaud.add(jLabel11, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLabel12.setText("Custom Baud To Set");
        jLabel12.setBounds(10, 50, 130, 20);
        lpBaud.add(jLabel12, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblBaud.setText("115200");
        lblBaud.setBounds(160, 50, 90, 20);
        lpBaud.add(lblBaud, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Bauds to Set"));

        bgSpeed.add(rbCstm);
        rbCstm.setText("Custom");
        rbCstm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customBaud(evt);
            }
        });

        bgSpeed.add(rbStd);
        rbStd.setSelected(true);
        rbStd.setText("Standard");
        rbStd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customBaud(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbCstm)
                    .addComponent(rbStd))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(rbStd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbCstm))
        );

        bCommOpen.setText("Open Comm Port");
        bCommOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onOpenEv(evt);
            }
        });

        bCommClose.setText("Close Comm Port");
        bCommClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onCloseEv(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lpBaud, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(lblSpeed))
                                .addGap(19, 19, 19)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbSpeed, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbPort, 0, 76, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(bCommOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCommClose, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(51, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSpeed)
                            .addComponent(cbSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(lpBaud, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bCommOpen, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bCommClose, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Comm Settings", jPanel1);

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Reset"));

        chkFullRst.setSelected(true);
        chkFullRst.setText("Full Reset");
        chkFullRst.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fullRstSelEv(evt);
            }
        });
        chkFullRst.setBounds(10, 20, 80, 23);
        jLayeredPane1.add(chkFullRst, javax.swing.JLayeredPane.DEFAULT_LAYER);

        chkSpecRst.setSelected(true);
        chkSpecRst.setText("Special Reset");
        chkSpecRst.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                specRstSelEv(evt);
            }
        });
        chkSpecRst.setBounds(100, 20, 110, 23);
        jLayeredPane1.add(chkSpecRst, javax.swing.JLayeredPane.DEFAULT_LAYER);

        bReset.setText("Go Reset");
        bReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                onRstEv(evt);
            }
        });
        bReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onGoRstEv(evt);
            }
        });
        bReset.setBounds(10, 50, 100, 23);
        jLayeredPane1.add(bReset, javax.swing.JLayeredPane.DEFAULT_LAYER);

        jLayeredPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Light"));

        bgLightGroup.add(rbLight50);
        rbLight50.setText("50 Hz");
        rbLight50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightSelected(evt);
            }
        });
        rbLight50.setBounds(20, 20, 60, 23);
        jLayeredPane2.add(rbLight50, javax.swing.JLayeredPane.DEFAULT_LAYER);

        bgLightGroup.add(rbLight60);
        rbLight60.setSelected(true);
        rbLight60.setText("60 Hz");
        rbLight60.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lightSelected(evt);
            }
        });
        rbLight60.setBounds(20, 40, 60, 23);
        jLayeredPane2.add(rbLight60, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(88, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Special Features", jPanel3);

        cbColType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2-bit Gray Scale", "4-bit Gray Scale", "8-bit Gray Scale", "8-bit Color", "12-bit Color", "16-bit Color", "JPEG" }));
        cbColType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ColTypeEv(evt);
            }
        });

        cbRAW.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "  80 x 60", "160 x 120", "320 x 240", "640 x 480", "128 x 128", "128 x 96" }));

        cbJPEG.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "  80 x 64", "160 x 128", "320 x 240", "640 x 480" }));

        jLabel4.setText("Color Type");

        lblRAW.setText("Raw Resolution");

        lblJPEG.setText("JPEG Resolution");

        sSkip.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));

        lblSkip.setText("Snapshot Skip Frames");

        lblPacket.setText("Packet Size");

        sPacket.setModel(new javax.swing.SpinnerNumberModel(200, 7, 999, 1));

        bgJPEGRAW.add(rbRAW);
        rbRAW.setSelected(true);
        rbRAW.setText("RAW");
        rbRAW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevPicRawJpeg(evt);
            }
        });

        bgRButton.add(rbSnap);
        rbSnap.setSelected(true);
        rbSnap.setText("Snapshot");
        rbSnap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PicTypeEv(evt);
            }
        });

        jLabel9.setText("Picture Type");

        chkVidMode.setText("Video Mode On");

        bgJPEGRAW.add(rbJPEG);
        rbJPEG.setText("JPEG");
        rbJPEG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevPicRawJpeg(evt);
            }
        });

        bgRButton.add(rbPrev);
        rbPrev.setText("Preview");
        rbPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PicTypeEv(evt);
            }
        });

        bGo.setText("Go!");
        bGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onGoEv(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lblSkip, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblJPEG)
                                .addComponent(lblRAW)
                                .addComponent(jLabel4)
                                .addComponent(lblPacket))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbRAW, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbColType, 0, 115, Short.MAX_VALUE)
                                    .addComponent(cbJPEG, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(sSkip, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(sPacket, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbSnap)
                            .addComponent(rbPrev))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkVidMode, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rbRAW)
                                    .addComponent(rbJPEG))
                                .addGap(49, 49, 49)
                                .addComponent(bGo, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbColType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRAW)
                    .addComponent(cbRAW, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblJPEG)
                    .addComponent(cbJPEG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPacket)
                    .addComponent(sPacket, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sSkip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSkip))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(rbSnap)
                    .addComponent(chkVidMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rbPrev)
                            .addComponent(rbRAW))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rbJPEG))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(bGo, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Pic Settings", jPanel2);

        jLabel3.setText("Terminal Log");

        jLabel14.setText("Picture Progress");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cimg, javax.swing.GroupLayout.PREFERRED_SIZE, 640, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pbPic, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14)
                            .addComponent(pbPic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(cimg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleDescription(null);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portSelected
        // TODO add your handling code here:
        taLog.append("COM" + cbPort.getItemAt(cbPort.getSelectedIndex()) + " selected.\n");
    }//GEN-LAST:event_portSelected

    private void speedSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speedSelected
        // TODO add your handling code here:
        taLog.append(cbSpeed.getItemAt(cbSpeed.getSelectedIndex()) + " bauds selected.\n");
    }//GEN-LAST:event_speedSelected

    private void customBaud(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customBaud
        // TODO add your handling code here:
        if(rbStd.isSelected() == true) {
            cbSpeed.setVisible(true);
            lblSpeed.setVisible(true);
            lpBaud.setVisible(false);
        } else {
            cbSpeed.setVisible(false);
            lblSpeed.setVisible(false);
            lpBaud.setVisible(true);
        }

    }//GEN-LAST:event_customBaud

    private void PicTypeEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PicTypeEv
        // TODO add your handling code here:
        lblSkip.setVisible(true);
        sSkip.setVisible(true);
        rbRAW.setVisible(false);
        rbJPEG.setVisible(false);
        if(rbPrev.isSelected() == true) {
            lblSkip.setVisible(false);
            sSkip.setVisible(false);
            rbRAW.setVisible(true);
            rbJPEG.setVisible(true);
            if(rbRAW.isSelected() == true) {
                cbColType.setSelectedIndex(0);
                lblSkip.setVisible(false);
                sSkip.setVisible(false);
                rbRAW.setVisible(true);
                rbJPEG.setVisible(true);
            }
        }
    }//GEN-LAST:event_PicTypeEv

    private void ColTypeEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ColTypeEv
        // TODO add your handling code here:
        if (cbColType.getSelectedIndex() == 0x06) { // selected JPEG
            lblJPEG.setVisible(true);
            cbJPEG.setVisible(true);
            lblPacket.setVisible(true);
            sPacket.setVisible(true);
            lblRAW.setVisible(false);
            cbRAW.setVisible(false);
            rbRAW.setSelected(false);
            if(rbRAW.isSelected() == true) {
                rbJPEG.setSelected(true);
            }
        } else {
            lblJPEG.setVisible(false);
            cbJPEG.setVisible(false);
            lblPacket.setVisible(false);
            sPacket.setVisible(false);
            lblRAW.setVisible(true);
            cbRAW.setVisible(true);
            rbRAW.setSelected(true);
        }
    }//GEN-LAST:event_ColTypeEv

    private void lightSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lightSelected
        // TODO add your handling code here:
        if(rbLight50.isSelected() == true) {
            taLog.append("Light filter at 50 Hz.\n");
        } else {
            taLog.append("Light filter at 60 Hz.\n");
        }

    }//GEN-LAST:event_lightSelected

    private void prevPicRawJpeg(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevPicRawJpeg
        // TODO add your handling code here:
        if(rbRAW.isSelected() == true) {
            cbColType.setSelectedIndex(0);
            cbJPEG.setVisible(false);
            lblJPEG.setVisible(false);
            cbRAW.setVisible(true);
            lblRAW.setVisible(true);
        } else {
            cbColType.setSelectedIndex(6);
            cbJPEG.setVisible(true);
            lblJPEG.setVisible(true);
            cbRAW.setVisible(false);
            lblRAW.setVisible(false);
        }
    }//GEN-LAST:event_prevPicRawJpeg

    private void onRstEv(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onRstEv
        // TODO add your handling code here:

    }//GEN-LAST:event_onRstEv

    private void fullRstSelEv(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fullRstSelEv
        // TODO add your handling code here:
        if (chkFullRst.isSelected() == true) {
            taLog.append("Full Reset Selected.\n");
        } else {
            taLog.append("State Machine Reset Selected.\n");
        }
    }//GEN-LAST:event_fullRstSelEv

    private void specRstSelEv(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_specRstSelEv
        // TODO add your handling code here:
        if(chkSpecRst.isSelected() == true) {
            taLog.append("Special Reset Selected.\n");
        } else {
            taLog.append("Special Reset Unselected.\n");
        }
    }//GEN-LAST:event_specRstSelEv

    private void bCommOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCommOpenActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_bCommOpenActionPerformed

    private void onGoEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onGoEv
        // TODO add your handling code here:
        byte FSMstate;
        byte port;
        short status;
        byte []initial;
        short []snapshot;
        byte colorType;
        byte RAWRes;
        byte JPEGRes;
        short pkgsize;
        byte snapType;
        short skipCtr;
        byte picType;
        short[] arr;
        byte type;
        byte special;
        byte light;


        FSMstate  = 0;
        status    = 0;
        port      = getPort();
        initial   = getInitial();
        pkgsize   = getPkgSize();
        snapshot  = getSnapshot();
        picType   = getPicType();
        colorType = initial[0];
        RAWRes    = initial[1];
        JPEGRes   = initial[2];
        snapType  = (byte) snapshot[0];
        skipCtr   = snapshot[1];
        type      = getType();
        special   = getSpecial();
        light     = getLight();
        cam.flush();
        if(bCommOpen.isEnabled() == false) {
            while(true) {
                switch(FSMstate) {
                    case 0:
                        status = cam.Conn(port);
                        if (status != 0x00) {
                            cam.Rst(port, type, special);
                            try {
                                Thread.sleep(4000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ViewForm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    case 1:
                        status = cam.Light(port, light);
                        break;
                    case 2:
                        status = cam.Initial(port, colorType, RAWRes, JPEGRes);
                        break;
                    case 3:
                        status = cam.SetPkgSize(port, pkgsize);
                        break;
                    case 4:
                        status = cam.Snapshot(port, snapType, skipCtr);
                        break;
                    case 5:
                        status = cam.GetPic(port, picType);
                        break;
                    case 6:
                        arr = new short[(int)cam.imgsize.LenByte];
                        for (int j = 0; j < cam.imgsize.LenByte; j++) {
                            arr[j] = cam.pkg.Img[j];
                        }
                        paint(arr);
                }
                if (status == 0x00) {
                    FSMstate = fsmCmd(++FSMstate);
                    if (FSMstate == 7) {
                        break;
                    }
                }
            }
        } else {
            taLog.append("First connect to COM port and select your settings.\n");
        }
    }//GEN-LAST:event_onGoEv

    private void onGoRstEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onGoRstEv
        // TODO add your handling code here:
        byte port;
        byte type;
        byte special;
        

        port    = getPort();
        type    = getType();
        special = getSpecial();
        if(bCommOpen.isEnabled() == false) {
                taLog.append("uCAM Reset\n");
                cam.Rst(port, type, special);
        } else {
            taLog.append("First Connect to COM port\n");
        }
    }//GEN-LAST:event_onGoRstEv

    private void onCloseEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onCloseEv
        byte port;


        port = (byte)(Integer.parseInt(cbPort.getItemAt(cbPort.getSelectedIndex()).toString()));
        cam.Dis(port);
        cam = null;
        bCommOpen.setEnabled(true);
        bCommClose.setEnabled(false);
        taLog.append("COM" + cbPort.getItemAt(cbPort.getSelectedIndex()) + " closed.\n");
    }//GEN-LAST:event_onCloseEv

    private void onOpenEv(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onOpenEv
        byte    port;
        int    bauds;


        port    = getPort();
        bauds   = getBauds();
        cam     = new uCAM(port, bauds);
        if(cam != null) {
            taLog.append("uCAM Connected\n");
            bCommClose.setEnabled(true);
            bCommOpen.setEnabled(false);
        } else {
           taLog.append("Error. Try to connect, again\n");
        }
        cam.flush();
    }//GEN-LAST:event_onOpenEv

    private void onCloseWin(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onCloseWin
        // TODO add your handling code here:
        cam.flush();
        onCloseEv(null);
    }//GEN-LAST:event_onCloseWin

    private void onClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onClick
        // TODO add your handling code here:
        if(bGo.isSelected() == true) {
            chkVidMode.setSelected(false);
        }
    }//GEN-LAST:event_onClick

    /**
    * @param args the command line arguments
    */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bCommClose;
    private javax.swing.JButton bCommOpen;
    private javax.swing.JButton bGo;
    private javax.swing.JButton bReset;
    private javax.swing.ButtonGroup bgJPEGRAW;
    private javax.swing.ButtonGroup bgLightGroup;
    private javax.swing.ButtonGroup bgRButton;
    private javax.swing.ButtonGroup bgSpeed;
    private javax.swing.JComboBox cbColType;
    private javax.swing.JComboBox cbJPEG;
    private javax.swing.JComboBox cbPort;
    private javax.swing.JComboBox cbRAW;
    private javax.swing.JComboBox cbSpeed;
    private javax.swing.JCheckBox chkFullRst;
    private javax.swing.JCheckBox chkSpecRst;
    private javax.swing.JCheckBox chkVidMode;
    private java.awt.Canvas cimg;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblBaud;
    private javax.swing.JLabel lblJPEG;
    private javax.swing.JLabel lblPacket;
    private javax.swing.JLabel lblRAW;
    private javax.swing.JLabel lblSkip;
    private javax.swing.JLabel lblSpeed;
    private javax.swing.JLayeredPane lpBaud;
    private javax.swing.JProgressBar pbPic;
    private javax.swing.JRadioButton rbCstm;
    private javax.swing.JRadioButton rbJPEG;
    private javax.swing.JRadioButton rbLight50;
    private javax.swing.JRadioButton rbLight60;
    private javax.swing.JRadioButton rbPrev;
    private javax.swing.JRadioButton rbRAW;
    private javax.swing.JRadioButton rbSnap;
    private javax.swing.JRadioButton rbStd;
    private javax.swing.JSpinner sDiv1;
    private javax.swing.JSpinner sDiv2;
    private javax.swing.JSpinner sPacket;
    private javax.swing.JSpinner sSkip;
    private javax.swing.JTextArea taLog;
    // End of variables declaration//GEN-END:variables

}