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
package org.routing.dijkstra;

import java.util.ArrayList;

public class PriorityList<E>
{

	ArrayList<E> objets;

	ArrayList<Double> priorites;

	int taille;

	public PriorityList()
	{
		objets = new ArrayList<E>();
		priorites = new ArrayList<Double>();
		taille = 0;
	}

	public boolean containsKey( E objet )
	{
		boolean contient = false;
		if( objets.contains( objet ) )
		{
			contient = true;
		}
		return contient;
	}

	public void insertion( E element, double prio )
	{
		boolean trouve = false;
		int max = priorites.size();
		int i = 0;
		while( ( !trouve ) && ( i < max ) )
		{
			if( priorites.get( i ) > prio )
			{
				trouve = true;
			}
			else
			{
				i++;
			}
		}
		if( i == max )
		{
			objets.add( element );
			priorites.add( prio );
		}
		else
		{
			objets.add( i, element );

			// MODIF ICI !
			// priorites.add(prio);
			priorites.add( i, prio );
		}
	}

	public boolean isEmpty()
	{
		boolean vide = false;
		if( objets.size() == 0 )
		{
			vide = true;
		}
		return vide;
	}

	public E lire( int position )
	{
		return objets.get( position );
	}

	public int size()
	{
		return objets.size();
	}

	public void suppression( E element )
	{
		int position = objets.lastIndexOf( element );
		objets.remove( position );
		priorites.remove( position );
	}

	@Override
	public String toString()
	{
		String laliste = new String( " -------- Liste --------- \n" );
		for( int i = 0; i < objets.size(); i++ )
		{
			laliste = laliste + objets.get( i ).toString() + ":::" + priorites.get( i ).toString() + "\n";
		}
		return laliste;
	}
}
