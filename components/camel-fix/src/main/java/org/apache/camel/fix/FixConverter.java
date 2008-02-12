package org.apache.camel.fix;

import quickfix.Message;
import quickfix.DataDictionary;
import quickfix.Group;
import quickfix.FieldMap;
import biz.c24.io.api.data.*;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.fix42.NewOrderSingle;
import biz.c24.io.fix42.NewOrderSingleElement;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.camel.Converter;

/**
 */
@Converter
public class FixConverter {
    @Converter
    public static Message convert(ComplexDataObject cdo) {
        Message msg = new Message();
        convert(cdo, msg);
        return msg;
    }

    private static void convert(ComplexDataObject cdo, FieldMap fm) {
        for (int i=0, max=cdo.getElementDeclCount(); i<max; i++) {
            Element e = cdo.getElementDecl(i);
            DataType t = e.getType();
            Object value = cdo.getElement(e.getName());
            if (value == null)
                continue;
            int tag = t.getName().startsWith("Field") ? tag(t) : cdo.getType().getName().startsWith("Field") ? tag(cdo.getType()) : 0;

            if (t instanceof ComplexDataType) {
                ComplexDataType ct = (ComplexDataType) t;
                if (ct.getName().startsWith("No") || ct.getName().endsWith("Header") || ct.getName().endsWith("Body") || ct.getName().endsWith("Trailer")) {
                    Element firstChild = ct.getElementDecl(0);
                    while (firstChild.getType() instanceof ComplexDataType && !firstChild.getType().getName().startsWith("Field"))
                        firstChild = ((ComplexDataType) firstChild.getType()).getElementDecl(0);
                    if (fm instanceof Message && ct.getName().endsWith("Header"))
                        convert((ComplexDataObject) value, ((Message) fm).getHeader());
                    if (fm instanceof Message && ct.getName().endsWith("Trailer"))
                        convert((ComplexDataObject) value, ((Message) fm).getTrailer());
                    else {
                        Group g = new Group(tag(firstChild.getType()), 0);
                        convert((ComplexDataObject) value, g); // recurse into child group and preserve hierarchy
                        fm.addGroup(g);
                    }
                }
                else
                    convert((ComplexDataObject) cdo.getElement(e.getName()), fm); // recurse into complex type and flatten
            }
            else if (t instanceof BooleanDataType)
                fm.setBoolean(tag, ((Boolean) value).booleanValue());
            else if (t instanceof CharDataType)
                fm.setChar(tag, ((Character) value).charValue());
            else if (t instanceof IntDataType)
                fm.setInt(tag, ((Integer) value).intValue());
            else if (t instanceof NumberDataType)
                fm.setDouble(tag, ((Number) value).doubleValue());
            else if (t instanceof StringDataType)
                fm.setString(tag, ((String) value));
            else if (t instanceof DateDataType) {
                DateDataType dt = (DateDataType) t;
                if (dt.isDate() && dt.isTime())
                    fm.setUtcTimeStamp(tag, ((Date) value)); // initiator for date / time fields is on parent
                else if (dt.isDate())
                    fm.setUtcDateOnly(tag, ((Date) value)); // initiator for date / time fields is on parent
                else if (dt.isTime())
                    fm.setUtcTimeOnly(tag, ((Date) value)); // initiator for date / time fields is on parent
                else
                    throw new IllegalArgumentException("Unknown date type "+dt.getName());
            }
            else
                throw new IllegalArgumentException("Unknown type "+e.getType().getName());
        }
    }

    private static int tag(DataType t) {
        String[] init = t.getInitiator();
        if (init == null || init.length == 0 || !init[0].endsWith("="))
            throw new IllegalArgumentException("Malformed initiator on "+t.getName());
        
        return Integer.valueOf(init[0].substring(0, init[0].length()-1)); // take tag from initiator, remove '=' first
    }

    private static String sort(String msg) {
        List<String> l = new ArrayList<String>(Arrays.asList(msg.split("\\x01")));
        Collections.sort(l);
        StringBuffer sb = new StringBuffer();
        for (String str : l)
            sb.append(str).append("|");

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Reading "+args[0]+"...");
        TextualSource src = new TextualSource(new FileInputStream(args[0]));
        ComplexDataObject cdo = src.readObject(NewOrderSingleElement.getInstance());

        System.out.println("Converting...");
        Message msg = convert(cdo);

        System.out.print("ArtixDS :");
        System.out.println(cdo.toString());
        System.out.print("QuickFix:");
        System.out.println(msg.toString());
        System.out.print("ArtixDS :");
        System.out.println(sort(cdo.toString()));
        System.out.print("QuickFix:");
        System.out.println(sort(msg.toString()));
    }
}
