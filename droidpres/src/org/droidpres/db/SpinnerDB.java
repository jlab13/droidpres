/*******************************************************************************
 * Copyright (c) 2010 Eugene Vorobkalo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eugene Vorobkalo - initial API and implementation
 ******************************************************************************/
package org.droidpres.db;

public class SpinnerDB {
	public int id;
	public String title;
	
	public SpinnerDB(int _id, String _title) {
		id = _id;
		title = _title;
	}
	
	public String toString(){
		return title;
	}
}
