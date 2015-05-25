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

import toxi.geom.Vec3D;
import toxi.math.MathUtils;

public class CurveFactory {

	static public LineStrip3D getPQKnot(int curve_length, int p, int q) {
		LineStrip3D vertices = new LineStrip3D();
		float theta = 0.1f;
		float dt = ((MathUtils.TWO_PI) / curve_length);
		
		for (int i=0; i<=curve_length+1; i++) {  
			
			float r = MathUtils.cos(q*theta) + 2;
			float x = r * MathUtils.cos(p * theta) * 50;
			float y = r * MathUtils.sin(p * theta) * 50;
			float z = -MathUtils.sin(q*theta) * 50;
			
			theta += dt;
			vertices.add(new Vec3D(x,y,z));
		}
		  
		return vertices;
	}
	
	static public LineStrip3D getCurve(int curve_length) {
		
		LineStrip3D vertices = new LineStrip3D();
		// points of a strange curve, circle, toroide, helix, whatever
		// parallel transport frame is a general approach
		float theta = 0.1f;
		//float p = 2.4f;
		//float q = 1.6f;

		//circle
		//float dt = ((MathUtils.TWO_PI) / curve_length);
		
		// viviani
		//float dt = ((MathUtils.TWO_PI) / curve_length)*2;
		
		// toroide
		float dt = ((MathUtils.TWO_PI) / curve_length);

		for (int i=0; i<=curve_length+1; i++) {  

			// circle
//			float x = cos(theta) * 30;
//			float y = sin(theta) * 30;
//			float z = 1;

			// viviani
//			float a = 50;
//			float x = a*(1+MathUtils.cos(theta));
//			float y = a*MathUtils.sin(theta);
//			float z = 2*a*MathUtils.sin( theta/2 );
			
			// torous
			float p = 3;
			float q = 1;
			float r = MathUtils.cos(q*theta) + 2;
			float x = r * MathUtils.cos(p * theta) * 50;
			float y = r * MathUtils.sin(p * theta) * 50;
			float z = -MathUtils.sin(q*theta) * 50;
			

			// variants
//			x = 20+ sin(theta)*40;
//			y = 20+ cos(theta)*40;
//			z = 20+ log(theta)*50;

			theta += dt;
			vertices.add(new Vec3D(x,y,z));
		}
		  
		return vertices;
	}
}