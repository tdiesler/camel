/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import biz.c24.io.api.data.BooleanDataType;
import biz.c24.io.api.data.CharDataType;
import biz.c24.io.api.data.ComplexDataObject;
import biz.c24.io.api.data.ComplexDataType;
import biz.c24.io.api.data.DataType;
import biz.c24.io.api.data.DateDataType;
import biz.c24.io.api.data.Element;
import biz.c24.io.api.data.IntDataType;
import biz.c24.io.api.data.NumberDataType;
import biz.c24.io.api.data.StringDataType;
import biz.c24.io.api.presentation.TextualSource;
import biz.c24.io.fix42.NewOrderSingleElement;

import org.apache.camel.Converter;
import org.apache.camel.converter.IOConverter;
import org.apache.camel.converter.ObjectConverter;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

/**
 */
@Converter
public final class FixConverter {
    private FixConverter() {
        // Helper class
    }
    @Converter
    public static Message convert(ComplexDataObject cdo) {
        Message msg = new Message();
        convert(cdo, msg);
        return msg;
    }

    @Converter
    public static ComplexDataObject convert(Message message) throws IOException {
        String text = message.toString();
        TextualSource src = new TextualSource(new StringReader(text));
        return src.readObject(NewOrderSingleElement.getInstance());
    }


    // A bunch of extra helper converters
    //-------------------------------------------------------------------------
    @Converter
    public static String toString(Message message) throws IOException {
        return message.toString();
    }

    @Converter
    public static InputStream toInputStream(Message message) throws IOException {
        return IOConverter.toInputStream(toString(message));
    }

    @Converter
    public static byte[] toByteArray(Message message) throws IOException {
        return ObjectConverter.toByteArray(toString(message));
    }

    @Converter
    public static Message convert(byte[] data) throws IOException {
        return convert(IOConverter.toInputStream(data));
    }

    @Converter
    public static Message convert(String data) throws IOException {
        return convert(IOConverter.toInputStream(data));
    }

    @Converter
    public static Message convert(URL data) throws IOException {
        return convert(IOConverter.toInputStream(data));
    }

    @Converter
    public static Message convert(BufferedReader reader) throws IOException {
        return convert(IOConverter.toInputStream(reader));
    }

    @Converter
    public static Message convert(File file) throws IOException {
        return convert(new FileInputStream(file));
    }

    @Converter
    public static Message convert(InputStream in) throws IOException {
        TextualSource src = new TextualSource(in);
        return convert(src.readObject(NewOrderSingleElement.getInstance()));
    }

    // Implementation details
    //-------------------------------------------------------------------------

    private static void convert(ComplexDataObject cdo, FieldMap fm) {
        int max = cdo.getElementDeclCount();
        for (int i = 0; i < max; i++) {
            Element e = cdo.getElementDecl(i);
            DataType t = e.getType();
            Object value = cdo.getElement(e.getName());
            if (value == null) {
                continue;
            }
            int tag = t.getName().startsWith("Field") ? tag(t) : cdo.getType().getName().startsWith("Field") ? tag(cdo.getType()) : 0;

            if (t instanceof ComplexDataType) {
                ComplexDataType ct = (ComplexDataType)t;
                if (ct.getName().startsWith("No") || ct.getName().endsWith("Header") || ct.getName().endsWith("Body") || ct.getName().endsWith("Trailer")) {
                    Element firstChild = ct.getElementDecl(0);
                    while (firstChild.getType() instanceof ComplexDataType && !firstChild.getType().getName().startsWith("Field")) {
                        firstChild = ((ComplexDataType)firstChild.getType()).getElementDecl(0);
                    }
                    if (fm instanceof Message && ct.getName().endsWith("Header")) {
                        convert((ComplexDataObject)value, ((Message)fm).getHeader());
                    }
                    if (fm instanceof Message && ct.getName().endsWith("Trailer")) {
                        convert((ComplexDataObject)value, ((Message)fm).getTrailer());
                    } else {
                        Group g = new Group(tag(firstChild.getType()), 0);
                        convert((ComplexDataObject)value, g); // recurse into
                                                              // child group and
                                                              // preserve
                                                              // hierarchy
                        fm.addGroup(g);
                    }
                } else {
                    convert((ComplexDataObject)cdo.getElement(e.getName()), fm); // recurse
                                                                                 // into
                                                                                 // complex
                                                                                 // type
                                                                                 // and
                                                                                 // flatten
                }
            } else if (t instanceof BooleanDataType) {
                fm.setBoolean(tag, ((Boolean)value).booleanValue());
            } else if (t instanceof CharDataType) {
                fm.setChar(tag, ((Character)value).charValue());
            } else if (t instanceof IntDataType) {
                fm.setInt(tag, ((Integer)value).intValue());
            } else if (t instanceof NumberDataType) {
                fm.setDouble(tag, ((Number)value).doubleValue());
            } else if (t instanceof StringDataType) {
                fm.setString(tag, (String)value);
            } else if (t instanceof DateDataType) {
                DateDataType dt = (DateDataType)t;
                if (dt.isDate() && dt.isTime()) {
                    fm.setUtcTimeStamp(tag, (Date)value); // initiator for
                                                            // date / time
                                                            // fields is on
                                                            // parent
                } else if (dt.isDate()) {
                    fm.setUtcDateOnly(tag, (Date)value); // initiator for date
                                                           // / time fields is
                                                           // on parent
                } else if (dt.isTime()) {
                    fm.setUtcTimeOnly(tag, (Date)value); // initiator for date
                                                           // / time fields is
                                                           // on parent
                } else {
                    throw new IllegalArgumentException("Unknown date type " + dt.getName());
                }
            } else {
                throw new IllegalArgumentException("Unknown type " + e.getType().getName());
            }
        }
    }

    private static int tag(DataType t) {
        String[] init = t.getInitiator();
        if (init == null || init.length == 0 || !init[0].endsWith("=")) {
            throw new IllegalArgumentException("Malformed initiator on " + t.getName());
        }
        return Integer.valueOf(init[0].substring(0, init[0].length() - 1)); // take
                                                                            // tag
                                                                            // from
                                                                            // initiator,
                                                                            // remove
                                                                            // '='
                                                                            // first
    }

    private static String sort(String msg) {
        List<String> l = new ArrayList<String>(Arrays.asList(msg.split("\\x01")));
        Collections.sort(l);
        StringBuffer sb = new StringBuffer();
        for (String str : l) {
            sb.append(str).append("|");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Reading " + args[0] + "...");
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
