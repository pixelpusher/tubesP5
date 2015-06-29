/*
 * Original from https://github.com/nkint/tubesP5
 * Originally by by Alberto Massa (https://twitter.com/nkint)
 * Processing 2.0 repack by Raphaël de Courville (https://github.com/nkint/tubesP5/blob/master/vimeo.com/sableraf)
 *
 * Modified for spirals and spline path profiles by Evan Raskob <info@pixelist.info>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package tubesP5.library;


import tubesP5.library.ParallelTransportFrame;
import toxi.geom.mesh.TriangleMesh;


public abstract class Tube extends TriangleMesh {

	protected ParallelTransportFrame soul;
	private int curveLength;
	protected int num_faces;
	private boolean computed = false;  // are we finished computing?
	
	
	//-------------------------------------------------------- ctor
	
	public Tube(ParallelTransportFrame soul) {
		//System.out.println("Tube > constructor");
		this.setComputed(false);
		this.soul = soul;
		this.curveLength = soul.getCurveLength();
		
		if(soul.getCurveLength()==0) return;
		
		//compute(); // removed, do it manually now
	}
	
	
	//-------------------------------------------------------- vertex computation

	public abstract void compute();
	

	public int getCurveLength() {
		return curveLength;
	}

	public void setCurveLength(int curveLength) {
		this.curveLength = curveLength;
	}


	public boolean isComputed() {
		return computed;
	}


	protected void setComputed(boolean computed) {
		this.computed = computed;
	}

	
}
