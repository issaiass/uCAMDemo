
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrador
 */




public class uCAM {
    public interface CMD_LIST{
        public static final short INITIAL          = 0x01;
        public static final short GET_PICTURE      = 0x04;
        public static final short SNAPSHOT         = 0x05;
        public static final short SET_PACKAGE_SIZE = 0x06;
        public static final short SET_BAUD_RATE    = 0x07;
        public static final short RESET            = 0x08;
        public static final short PWR_OFF          = 0x09;
        public static final short DATA             = 0x0A;
        public static final short SYNC             = 0x0D;
        public static final short ACK              = 0x0E;
        public static final short NAK              = 0x0F;
        public static final short LIGHT            = 0x13;
    }
    public interface COLOR_TYPE {
        public static final short GS_2B     = 0x01;
        public static final short GS_4B     = 0x02;
        public static final short GS_8B     = 0x03;
        public static final short COLOR_8B  = 0x04;
        public static final short COLOR_12B = 0x05;
        public static final short COLOR_16B = 0x06;
        public static final short JPEG      = 0x07;
    }
    public interface RAW_RES {
        public static final short RAW80X60   = 0x01;
        public static final short RAW160X120 = 0x03;
        public static final short RAM320X240 = 0x05;
        public static final short RAW640X480 = 0x07;
        public static final short RAW128X128 = 0x09;
        public static final short RAW128X96  = 0x0B;
    }
    public interface JPEG_RES {
        public static final short JPEG80X64   = 0x01;
        public static final short JPEG160X128 = 0x03;
        public static final short JPEG320X240 = 0x05;
        public static final short JPEG640X480 = 0x07;
    }
    public interface PICTURE_TYPE {
        public static final short SNAPSHOT_TYPE     = 0x01;
        public static final short RAW_PREVIEW_TYPE  = 0x02;
        public static final short JPEG_PREVIEW_TYPE = 0x05;
    }
    public interface SNAPSHOT_TYPE {
        public static final short CMP_JPEG  = 0x00;
        public static final short UNCMP_RAW = 0x01;
    }
    public interface RESET_TYPE {
        public static final short RST_SYS     = 0x00;
        public static final short RST_FSM     = 0x01;
        public static final short RST_SPECIAL = 0xFF;
    }
    public interface ERR_NUM {
        public static final short OK                 = 0x00;
        public static final short PIC_TYPE           = 0x01;
        public static final short PIC_UP_SCALE       = 0x02;
        public static final short PIC_SCALE          = 0x03;
        public static final short UNEXP_REPLY        = 0x04;
        public static final short SEND_PIC_TO        = 0x05;
        public static final short UNEXP_CMD          = 0x06;
        public static final short SRAM_JPEG_TYPE     = 0x07;
        public static final short SRAM_JPEG_SIZE     = 0x08;
        public static final short PIC_FORMAT         = 0x09;
        public static final short PIC_SIZE           = 0x0A;
        public static final short PARAM              = 0x0B;
        public static final short SND_REG_TO         = 0x0C;
        public static final short CMD_ID             = 0x0D;
        public static final short PIC_NRDY           = 0x0E;
        public static final short TXFER_PKG_NUM      = 0x0F;
        public static final short SET_TXFER_PKG_SIZE = 0x10;
        public static final short CMD_HDR            = 0xF0;
        public static final short CMD_LEN            = 0xF1;
        public static final short ERR                = 0xF2;
        public static final short SND_PIC            = 0xF5;
        public static final short SND_CMD            = 0xFF;
    }
    public interface LIGHT_TYPE {
        public static final short LIGHT_50 = 0x00;
        public static final short LIGHT_60 = 0x01;
    }

    public class Package {
        byte []ID       = new byte[2];     // holds id command
        byte []DataSize = new byte[2];     // holds data size of a frame
        byte []Img;     // pointer of the image
        byte []VeriCode = new byte[2];     // verification code of the frame (checksum)
                               // aditional, not in package
        char   Type;     // holds picture type (snapshot, current picture)

        public Package() {
            ID       = new byte[2];     // holds id command
            DataSize = new byte[2];     // holds data size of a frame
            Img      = new byte[800*600];     // pointer of the image
            VeriCode = new byte[2];     // verification code of the frame (checksum)                       // aditional, not in package
            Type     = 0;     // holds picture type (snapshot, current picture)
        }

    }                     // general package definition

    public class ImgSize {
        long LenByte;        // total lenght of the image
        byte LenByte0; // byte 0 of image size data
        byte LenByte1; // byte 1 of image size data
        byte LenByte2; // byte 2 of image size data
        byte []Byte;   // byte array of image data

        public ImgSize() {
            LenByte  = 0;
            LenByte0 = 0;
            LenByte1 = 0;
            LenByte2 = 0;
            Byte     = new byte[3];
        }
    }                  // size of image
    
    private static final short ACCESS_TIME = 4000;
    short pkgsize;
    private byte isRAW;
    private static byte rgb_raw;
    Package     pkg;
    ImgSize imgsize;
    SimpleSerial ss;


    public String HexToString(byte hex){
        return Integer.toString((hex & 0xFF) + 0x100, 16).substring(1).toUpperCase();
    }

    public uCAM () {
        ss      = new SimpleSerialNative(3,115200,8,0,0);
        pkg     = new Package();
        imgsize = new ImgSize();
    }
    public uCAM (byte port, int baud) {
        ss  = new SimpleSerialNative(port, baud, 8, 0, 0);
        pkg = new Package();
        imgsize = new ImgSize();
    }

    short Conn(byte port) {    // connects
        byte i;                                      // counter
        byte []cmd;                                 // command array
        short status;                                 // status of SYNC command


        cmd    = new byte[6];
        i      = 0;
        status = ERR_NUM.ERR;
        cmd[0] = (byte)0xAA;                                // SYNC command
        cmd[1] = (byte)0x0D;
        cmd[2] = (byte)0x00;
        cmd[3] = (byte)0x00;
        cmd[4] = (byte)0x00;
        cmd[5] = (byte)0x00;
     //   if(ss != null) {  // connection OK?
            System.out.println("Connected\n");                    // prints message
            for (i = 0; i < 60; i++) {                // wait a sync confirmation
                status = Cmd(port, cmd);           // send command
                if (status == ERR_NUM.OK) {                   // breaks if ok
                    break;
                }
            }
       /* } else {
            System.out.println("Port Busy...\n");
            while(true) {
                if (ss != null) {
                    break;
                }
            }
        }*/
        return status;                                // return status of SYNC
    }

    short Initial(byte port, byte color, byte RAWres, byte JPEGres) {
        byte []cmd = new byte[6];                // command array


        cmd[0] = (byte)0xAA;               // INITIAL command
        cmd[1] = 0x01;
        cmd[2] = 0x00;
        cmd[3] = color;              // type of color
        cmd[4] = RAWres;             // type of JPEG (  raw  ) resolution
        cmd[5] = JPEGres;            // type of JPEG (not raw) resolution
        return Cmd(port, cmd);    // send command and cheks, return status of cmd
    }

    short SetPkgSize(byte port, short pkg) {
        byte []cmd = new byte[6];                  // command array

        pkgsize = pkg;                 // for private use in uCAMCmd function
        cmd[0]  = (byte)0xAA;                // SET_PACKAGE command
        cmd[1]  = 0x06;
        cmd[2]  = 0x08;
        cmd[3]  = (byte)(pkg);        // low byte package size
        cmd[4]  = (byte)(pkg >> 8);   // high byte package size
        cmd[5]  = 0x00;
        return Cmd(port, cmd);      // send command, return status of command
    }

    short Snapshot(byte port, byte type, short skipframe) {
        byte []cmd = new byte[6];                        // the command array


        cmd[0] = (byte)0xAA;                       // SNAPSHOT command
        cmd[1] = 0x05;
        cmd[2] = type;
        cmd[3] = (byte)(skipframe);         // skípframe low byte
        cmd[4] = (byte)(skipframe >> 8);    // skipframe high byte
        cmd[5] = 0x00;
        return Cmd(port, cmd);            // send command, returns the status
    }

    short GetPic(byte port, byte type) {
        byte []cmd = new byte[6];               // array for command


        cmd[0] = (byte)0xAA;              // GET_PICTURE
        cmd[1] = 0x04;
        cmd[2] = type;
        cmd[3] = 0x00;
        cmd[4] = 0x00;
        cmd[5] = 0x00;
        return Cmd(port, cmd);   // return status of command
    }

    short Light(byte port, byte type) {
        byte []cmd = new byte[6];


        cmd[0] = (byte)0xAA;             // LIGHT command
        cmd[1] = 0x13;
        cmd[2] = type;             // 50 or 60 Hz command of light
        cmd[3] = 0x00;
        cmd[4] = 0x00;
        cmd[5] = 0x00;
        return Cmd(port, cmd);  // returs command status
    }

    short SetBaud(byte port, short divider) {
        byte []cmd = new byte[6];


        cmd[0] = (byte)0xAA;                   // SET_BAUD_RATE command
        cmd[1] = 0x07;
        cmd[2] = (byte)(divider >> 8);  // divider 1
        cmd[3] = (byte)(divider);       // divider 2
        cmd[4] = 0x00;
        cmd[5] = 0x00;
        return Cmd(port, cmd);        // return status of command
    }

    short Rst(byte port, byte type, byte special) {
        byte []cmd = new byte[6];


        cmd[0] = (byte)0xAA;             // RESET command
        cmd[1] = 0x08;
        cmd[2] = type;
        cmd[3] = 0x00;
        cmd[4] = 0x00;
        cmd[5] = special;
        return Cmd(port, cmd);   // returns command status
    }

    short PwrOff(byte port) {
        byte []cmd = new byte[6];              // command array


        cmd[0] = (byte)0xAA;             // PWR_OFF command
        cmd[1] = 0x09;
        cmd[2] = 0x00;
        cmd[3] = 0x00;
        cmd[4] = 0x00;
        cmd[5] = 0x00;
        return Cmd(port, cmd);  // returs command status
    }

    void Send(byte port, byte []cmd) throws InterruptedException {
        byte i;
        String str;


        isRAW(cmd);
        System.out.print("TX -> ");                                     // prints Tx direction
        for(i = 0; i < 6; i++) {                              // send 6 chars
            System.out.print("0x");  // print char to send
            str = String.valueOf(cmd[i]);
            System.out.print(this.HexToString(cmd[i])); //(cmd[i]));
            ss.writeByte(cmd[i]);                   // send the char
            System.out.print(" ");
        }
        Thread.sleep(60);
        System.out.println();                                         // separate line
    }

    short Ack(byte port, int pid) {
        byte []cmd = new byte[6];                // command array


        cmd[0] = (byte)0xAA;               // ACK command
        cmd[1] = 0x0E;
        cmd[2] = 0x00;
        cmd[3] = 0x00;
        cmd[4] = (byte)(pid);       // pid  low byte
        cmd[5] = (byte)(pid >> 8);  // pid high byte
        try {
            Send(port, cmd); // SEND command
        } catch (InterruptedException ex) {
            Logger.getLogger(uCAM.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERR_NUM.OK;                   // returns OK
    }

    void Dis(byte port) {
        Rst(port, (byte)(RESET_TYPE.RST_SYS), (byte)(RESET_TYPE.RST_SPECIAL));  // RESET command
        ss.close();                   // disconnect from serial port
        ss = null;                           // delete com port usage
        flush();
    }



    void flush() {
        byte b;
        while(ss.available() > 0) {
            b = (byte)ss.readByte();
        }
    }

    void ErrLog(short err) {
        switch(err) {                            // switch to print a specific error
            case ERR_NUM.PIC_TYPE:
                System.out.println("Picture Type Error\n");
                break;
            case ERR_NUM.PIC_UP_SCALE:
                System.out.println("Picture Up Scale\n");
                break;
            case ERR_NUM.PIC_SCALE:
                System.out.println("Picture Scale Error\n");
                break;
            case ERR_NUM.UNEXP_REPLY:
                System.out.println("Unexpected Reply\n");
                break;
            case ERR_NUM.SEND_PIC_TO:
                System.out.println("Send Picture Timeout\n");
                break;
            case ERR_NUM.UNEXP_CMD:
                System.out.println("Unexpected Command\n");
                break;
            case ERR_NUM.SRAM_JPEG_TYPE:
                System.out.println("SRAM JPEG Type Error\n");
                break;
            case ERR_NUM.SRAM_JPEG_SIZE:
                System.out.println("SRAM JPEG Size Error\n");
                break;
            case ERR_NUM.PIC_FORMAT:
                System.out.println("Picture Format Error\n");
                break;
            case ERR_NUM.PIC_SIZE:
                System.out.println("Picture Size Error\n");
                break;
            case ERR_NUM.PARAM:
                System.out.println("Parameter Error\n");
                break;
            case ERR_NUM.SND_REG_TO:
                System.out.println("Send Register Timeout\n");
                break;
            case ERR_NUM.CMD_ID:
                System.out.println("Command ID Error\n");
                break;
            case ERR_NUM.PIC_NRDY:
                System.out.println("Picture Not Ready\n");
                break;
            case ERR_NUM.TXFER_PKG_NUM:
                System.out.println("Transfer Package Number Error\n");
                break;
            case ERR_NUM.SET_TXFER_PKG_SIZE:
                System.out.println("Set Transfer Package Size Wrong\n");
                break;
            case ERR_NUM.CMD_HDR:
                System.out.println("Command Header Error\n");
                break;
            case ERR_NUM.CMD_LEN:
                System.out.println("Command Lenght Error\n");
                break;
            case ERR_NUM.SND_PIC:
                System.out.println("Send Picture Error\n");
                break;
            case ERR_NUM.SND_CMD:
                System.out.println("Send Command Error\n");
                break;
            case ERR_NUM.ERR:
                System.out.println("General Error\n");
                break;
        }
    }

    short CmdChk(byte []txcmd, byte []rxcmd) {
        byte i;                                               // holds the index
        short status;                                          // holds the status


        status = ERR_NUM.OK;                                           // status OK
        System.out.print("RX -> ");                                      // print message
        for (i = 0; i < 6; i++) {                              // get six bytes
            switch (i) {
                case 1:
                    System.out.print("0x" + this.HexToString(rxcmd[1]) + " ");   // print received
                    if (txcmd[1] == rxcmd[2]) {                // if ACK received
                        status = ERR_NUM.OK;                           // asserted
                    } else if (rxcmd[1] == CMD_LIST.NAK ) {             // if NAK
                        status = ERR_NUM.ERR;                          // error
                    } else if (rxcmd[1] == CMD_LIST.DATA) {             // if DATA
                        status = ERR_NUM.OK;                           // OK
                    } else if (rxcmd[1] == CMD_LIST.SYNC) {             // if SYNC
                        status = ERR_NUM.OK;                           // OK
                    } else {
                        status = ERR_NUM.ERR;                          // status is error
                    }
                    break;
                case 2:
                    System.out.print("0x" +this.HexToString(rxcmd[i]) + " ");   // print received
                    break;
                case 3:
                    System.out.print("0x" + this.HexToString(rxcmd[i]) + " ");   // print received
                    if (rxcmd[1] == CMD_LIST.DATA) {                    // if cmd is data
                        imgsize.LenByte0 = (byte)rxcmd[3]; // image size byte 0
                    }
                    break;
                case 4:
                    System.out.print("0x" + this.HexToString(rxcmd[4]) + " ");   // print hex
                    if(rxcmd[1] == CMD_LIST.NAK) {                      // if NAK
                        status = rxcmd[4];                     // get received error
                    } else if (rxcmd[1] == CMD_LIST.DATA) {             // if DATA
                        imgsize.LenByte1 = rxcmd[4]; // image size byte 1
                        status = ERR_NUM.OK;                           // status OK
                    }
                    break;
                case 5:
                    System.out.print("0x" + this.HexToString(rxcmd[5]) + " ");   // print hex
                    if (rxcmd[1] == CMD_LIST.DATA) {                    // if DATA
                        imgsize.LenByte2 = rxcmd[5]; // img size byte 2
                        status = ERR_NUM.OK;                           // status OK
                    }
                    break;
                default:
                    System.out.print("0x" + this.HexToString(rxcmd[i]) + " ");   // print hex
                    if (txcmd[i] == rxcmd[i]) {                // if tx is rx
                        status = ERR_NUM.OK;                           // OK
                    } else {
                        status = ERR_NUM.ERR;                          // otherwise, error
                    }
                    break;
            }
        }
        System.out.println();
        ErrLog(status);                                    // log error
        return status;                                         // return status
    }

    @SuppressWarnings("empty-statement")
    short Cmd(byte port, byte []cmd) {
        int           i;
        long        img;
        int    datasize;
        short    status;                                                                                  // the current command status                                                                               // action to issue
        byte     chksum;                                                                                  // checksum (calculated)
        byte      []rxd;                                                                                  // received frame
        short       ctr;                                                                                  // image counter
        short        ix;                                                                                  // index of buffer
        short      npkg;
        byte     action;
        int           n;
        byte        pix;
        int       nbyte;


        i      = 0;
        n      = 0;
        rxd    = new byte[12];
        status = ERR_NUM.ERR;
        try {
            Send(port, cmd);
        } catch (InterruptedException ex) {
            Logger.getLogger(uCAM.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (ss.available() > 0) {
            if (cmd[1] == CMD_LIST.SYNC || cmd[1] == CMD_LIST.GET_PICTURE) {
                while (i < 12) {
                    rxd[i++] = (byte)ss.readByte();
                }
            } else {
                while (i < 6) {
                    rxd[i++] = (byte)ss.readByte();
                }
            }
            status = CmdChk(cmd, rxd);
            if(status == ERR_NUM.OK && (cmd[1] == CMD_LIST.SYNC || cmd[1] == CMD_LIST.GET_PICTURE)) {
                rxd[0] = rxd[6];
                rxd[1] = rxd[7];
                rxd[2] = rxd[8];
                rxd[3] = rxd[9];
                rxd[4] = rxd[10];
                rxd[5] = rxd[11];
                rxd[6] = 0;
                status = CmdChk(cmd, rxd);   // check second frame received
                if (status == ERR_NUM.OK) {                     // if status OK
                    if (cmd[1] == CMD_LIST.SYNC) {
                        try {
                            Send(port, cmd); // send command
                        } catch (InterruptedException ex) {
                            Logger.getLogger(uCAM.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            Thread.sleep(ACCESS_TIME); // Wait to access AGC AEC circuits
                        } catch (InterruptedException ex) {
                            Logger.getLogger(uCAM.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (cmd[1] == CMD_LIST.GET_PICTURE) {
                        img  = (long)(rxd[3] & 0xFF) + (long)((rxd[4] & 0xFF) << 8) + (long)((rxd[5] & 0xFF) << 16);
                        imgsize.LenByte = img;
                        System.out.println("Image Size is = " + String.valueOf(imgsize.LenByte));
                        if(isRAW(cmd) == 1) {
                            i = 0;
                            ix = 0;                     // dynamically alocate a buffer
                            if (pkg.Img == null) {
                                System.out.println("could not allocate space\n");
                            }
                            while(ss.available() < 3*img/5);
                            while(ss.available() > 0) {
                                nbyte = ss.available();
                                pix = (byte) ss.readByte();
                                pkg.Img[i] = pix;
                                System.out.print(this.HexToString(pkg.Img[i]));
                                if (ix == 200) {
                                    ix=0;
                                    System.out.println();
                                }
                                ix++;
                                i++;
                            }
                            System.out.println();
                            Ack(port, (byte)0);
                        } else {
                            npkg = (short) (img / (pkgsize - 6));
                            if (img%(pkgsize - 6) > 0) {  // number of packages
                                npkg++;             // one more package
                            }
                            i = 0;
                            System.out.println("Number of packages = " + String.valueOf(npkg));
                            while(i < npkg) {       // up to max packages
                                Ack(port, i);         // try to send data after ACK command
                            /*    try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(uCAM.class.getName()).log(Level.SEVERE, null, ex);
                                }*/
                                pkg.ID[0]        = (byte) ss.readByte();     // id
                                pkg.ID[1]        = (byte) ss.readByte();
                                pkg.DataSize[0]  = (byte) ss.readByte();     // size of data frame
                                pkg.DataSize[1]  = (byte) ss.readByte();
                                datasize = (pkg.DataSize[1] << 8) + pkg.DataSize[0];  // lenght of image frame
                                chksum   = 0; // reset checksum for recalculating it
                                chksum   = (byte)(pkg.ID[0] + pkg.ID[1] + pkg.DataSize[0] + pkg.DataSize[1]); // calculate image size
                                System.out.print("uCAMID = 0x" + String.valueOf(pkg.ID[0]));
                                System.out.println(String.valueOf(pkg.ID[1]));
                                System.out.println("uCAMData = " + String.valueOf(datasize));
                                if (pkg.Img == null) {
                                    System.out.println("could not allocate space");
                                }
                                ix  = 0;
                                while(n < datasize) {                                              // wait bytes of image (a lot)
                                    n = ss.available();                                                 // count bytes
                                }
                                ctr = 0;
                                System.out.println("Number of Bytes = \nRX -> " + String.valueOf(n));
                                while(ix < datasize) {
                                    pix = (byte) ss.readByte();
                                    pkg.Img[ix] = pix;
                                    chksum  = (byte) (chksum + pix); // calculating checksum
                                    System.out.println("0x" + String.valueOf(n));
                                    ix++;
                                }
                                System.out.println();
                                pkg.VeriCode[0] = (byte) ss.readByte();                           // get verification code
                                pkg.VeriCode[1] = (byte) ss.readByte();
                                if (pkg.VeriCode[0] != chksum) {
                                    ErrLog(ERR_NUM.SND_PIC);
                                    while(true) {
                                        ;
                                    }
                                }
                                i++;
                            }
                            Ack(port, 0xF0F0);    // last package ACK
                        }
                    }
                }
            }
        }
        return status;
    }

    short isRAW(byte []cmd) {



  //      rgb_raw = rgb_raw;                   // rgbraw
        if (cmd[1] == CMD_LIST.INITIAL) {             // if initial
            if (cmd[3] == COLOR_TYPE.JPEG) {            // and command JPEG
                isRAW = (1 << 0);            // 1
            } else {                         // otherwise
                isRAW = (1 << 1);            // 2
            }
        }
        if (cmd[1] == CMD_LIST.SNAPSHOT) {            // if snapshot
            if (cmd[2] == SNAPSHOT_TYPE.CMP_JPEG) {        // JPEG snapshot?
                isRAW |= (1 << 2);           // 4
            } else {                         // otherwise
                isRAW |= (1 << 3);           // 8
            }
        }
        if (cmd[1] == CMD_LIST.GET_PICTURE) {         // if a command picture
            switch(cmd[2]) {                 // verify type of command
                case PICTURE_TYPE.JPEG_PREVIEW_TYPE:      // if is a JPEG Preview
                    isRAW |= (1 << 5);       // 32
                    break;
                case PICTURE_TYPE.SNAPSHOT_TYPE:          // if is a Snapshot Preview
                    isRAW |= (1 << 4);       // 16
                    break;
                case PICTURE_TYPE.RAW_PREVIEW_TYPE:       // if is a raw picture
                    isRAW |= (1 << 6);       // 64
            }
        }
        switch(isRAW) {                      // switch type
            case 21:                         // JPEG
            case 33:
            case 37:                         // not RGB, JPEG
                rgb_raw = 0;
                break;
            case 26:                         // RAW
            case 34:
            case 66:
            case 74:
                rgb_raw = 1;                 // is RGB
                break;
            default:
                rgb_raw = (byte) ERR_NUM.ERR;               // an error ocurred
                break;
        }
        return rgb_raw;                      // return a boolean, raw or jpeg
    }

}
