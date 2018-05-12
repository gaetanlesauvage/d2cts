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
package org.vehicles.models;

import java.util.HashMap;

public class StraddleCarrierModels {
	private static StraddleCarrierModels instance;
	private static Object objetSynchrone__ = new Object();
	public static StraddleCarrierModels getInstance (){
		if(instance == null) 	{
			synchronized (objetSynchrone__) {
				if(instance == null) instance = new StraddleCarrierModels();
			}
		}
		return instance;
	}
	
	private HashMap<String , StraddleCarrierModel> models;
	
	private StraddleCarrierModels (){
		models = new HashMap<String, StraddleCarrierModel>();
	}
	
	public void add(StraddleCarrierModel model){
		models.put(model.getId(), model);
	}
	
	public StraddleCarrierModel getModel(String id){
		return models.get(id);
	}
	
	
}
