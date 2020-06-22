package com.energyxxer.trident.ui.editor.behavior;

import com.energyxxer.util.logger.Debug;

import java.awt.datatransfer.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MultiStringSelection implements Transferable, ClipboardOwner {

    public static final DataFlavor multiStringFlavor = new DataFlavor(String[].class, "String Array");

    private static DataFlavor[] flavors;

    private String[] plainTextData = null;
    private String[] htmlTextData = null;

    static {
        ArrayList<DataFlavor> flavors = new ArrayList<>();
        flavors.add(multiStringFlavor);
        flavors.add(DataFlavor.stringFlavor);
        try {
            for (String m : new String[]{"text/plain", "text/html"}) {
                flavors.add(new DataFlavor(m + ";class=java.lang.String"));
                flavors.add(new DataFlavor(m + ";class=java.io.Reader"));
                flavors.add(new DataFlavor(m + ";class=java.io.InputStream;charset=utf-8"));
            }
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        MultiStringSelection.flavors = flavors.toArray(new DataFlavor[0]);
    }

    public MultiStringSelection(String[] plainTextData, String[] htmlTextData) {
        this.plainTextData = plainTextData;
        this.htmlTextData = htmlTextData;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for(DataFlavor f : flavors) {
            if(f.equals(flavor)) return true;
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if(flavor.equals(multiStringFlavor)) {
            return plainTextData;
        } else {
            StringBuilder sb = null;
            if (flavor.getMimeType().contains("text/plain") || flavor.equals(DataFlavor.stringFlavor)) {
                sb = new StringBuilder();
                for(String str : plainTextData) {
                    sb.append(str);
                }
            } else if (flavor.getMimeType().contains("text/html")) {
                sb = new StringBuilder();
                sb.append("<html><body>");
                for(String str : htmlTextData) {
                    sb.append(str);
                }
                sb.append("</body></html>");
            }

            if (sb != null) {
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return sb.toString();
                }
                else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(sb.toString());
                }
                else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            Debug.log("ruh roh");
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
