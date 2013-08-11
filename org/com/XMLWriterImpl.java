/*
 * This file is part of D²CTS : Dynamic and Distributed Container Terminal Simulator.
 *
 * Copyright (C) 2009-2012  Gaëtan Lesauvage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.com;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.display.TextDisplay;
import org.util.RecordableObject;

public class XMLWriterImpl implements RecordableObject {
	private static XMLWriterImpl instance;
	private static final Logger log = Logger.getLogger(XMLWriterImpl.class);

	private String id;

	private TextDisplay out;
	private PrintWriter pwMessages;
	public static String host = "";
	public static String uriDisplay = "";
	public static final String rmiBindingName = "XMLWriter";
	public static String url = "./xmlMessages.xml";

	private XMLWriterImpl() {
		this.id = rmiBindingName;
		try {
			pwMessages = new PrintWriter(url);
		} catch (FileNotFoundException e) {
			log.fatal(e.getMessage(), e);
		}
	}

	private XMLWriterImpl(String id, String urlMessageFile) {
		this(id, urlMessageFile, null);
	}

	private XMLWriterImpl(String id, String urlMessageFile, TextDisplay out) {
		this.id = id;
		try {
			pwMessages = new PrintWriter(urlMessageFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		setTextDisplay(out);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public TextDisplay getTextDisplay() {
		return out;
	}

	@Override
	public void setTextDisplay(TextDisplay out) {
		this.out = out;
	}

	public void write(String msg) {
		pwMessages.append(msg);
		pwMessages.flush();
	}

	public static XMLWriterImpl getInstance() {
		if (instance == null) {
			instance = new XMLWriterImpl();
		}
		return instance;
	}

	public void destroy() {
		host = null;
		uriDisplay = null;
		url = null;
		id = null;
		out = null;
		pwMessages = null;
	}

}
