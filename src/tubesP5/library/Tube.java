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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tubesP5.library.ParallelTransportFrame;
import toxi.geom.LineStrip2D;
import toxi.geom.Vec3D;
import toxi.geom.Spline2D; // for path profiles
import toxi.geom.Vec2D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.math.MathUtils;

public class Tube extends TriangleMesh {

	private ParallelTransportFrame soul;
	private int curveLength;
	private float radius = 10;
	private int diameterQuality = 20; // also spline subdivisions
	private float[] cachedRadius = null;
	private boolean usedCachedRadius = false;
	
	private List< List<Vec3D> > circles = new ArrayList<List<Vec3D>>();
	private int num_faces;
	private boolean computed = false;  // are we finished computing?
	
	// for spline profiles
	public int splineQuality = 5;
	
	
	//-------------------------------------------------------- ctor
	
	public Tube(ParallelTransportFrame soul, float radius, int diameter_quality) {
		System.out.println("Tube > constructor");
		this.setComputed(false);
		this.soul = soul;
		this.curveLength = soul.getCurveLength();
		this.setRadius(radius);
		this.diameterQuality = diameter_quality;
		
		if(soul.getCurveLength()==0) return;
		
		//compute(); // removed, do it manually now
	}
	
	
	//-------------------------------------------------------- vertex computation

	public void compute() 
	{
		this.setComputed(false);
		
		num_faces = 0;
		
		List<Vec3D> circle1, circle2;
		float radius;
		
		radius = (isUsedCachedRadius() ? cachedRadius[0]: getRadius());
		circle1 = getCircle(0, radius);
		for(int i=1; i<curveLength-1; i++) {
			radius = (isUsedCachedRadius() ? cachedRadius[i]: getRadius());
			circle2 = getCircle(i, radius);
			
			addCircles(circle1, circle2);
			
			circle1 = circle2;
		}
		this.setComputed(true);
	}
	
	
	//
	// convenience function for splines
	//
	public LineStrip2D computeAndGetSplineVerts( Spline2D spline)
	{
		return spline.toLineStrip2D(this.diameterQuality);
		//return spline.getDecimatedVertices(this.splineQuality, true);
	}
	
	
	// TODO - change exception to a proper type
	
	public void compute( List<Spline2D> profiles) //throws Exception
	{
		this.setComputed(false);
		
		this.num_faces = 0;
		
		// clear all faces
		this.faces.clear();
		
		// sanity check
		/*
		if (profiles.size() != soul.getCurveLength())
		{
			throw new Exception("ERROR profile and curve not same length: " + profiles.size() + "/" 
					+ soul.getCurveLength());
		}
		*/
		
		Iterator<Spline2D> profilesIterator = profiles.iterator();

		Spline2D currentShape = profilesIterator.next();
				
		LineStrip2D currentShapeVerts = computeAndGetSplineVerts(currentShape);

		int i=0; // index

		ArrayList<Vec3D> currentPath = shapeOnPath(currentShapeVerts, soul, i);
		
		while (++i < soul.getCurveLength())
		{
			//System.out.println("profile pts:" + currentShapeVerts.size());
			//System.out.println("soul[" + i + "]=" +  soul.get(i));
			
			Spline2D nextShape = profilesIterator.next();
			LineStrip2D nextShapeVerts = computeAndGetSplineVerts(nextShape);
			ArrayList<Vec3D> nextPath = shapeOnPath(nextShapeVerts, soul, i);

			// blindly try... so what if we hit an exception... catch it later


			int endIndex = currentPath.size() - 2;
			
			for (int startIndex = 0; startIndex < endIndex; startIndex++  )
			{

//				Vec3D prevProfileVert1 = currentPath.get(startIndex);
//				Vec3D prevProfileVert2 = currentPath.get(startIndex+1);
//				Vec3D currProfileVert1 = nextPath.get(startIndex);
//				Vec3D currProfileVert2 = nextPath.get(startIndex+1);
//
//				this.addFace(prevProfileVert1, prevProfileVert2, currProfileVert1);
//				this.addFace(prevProfileVert2, currProfileVert2, currProfileVert1);				

				Vec3D  p1, p2, p3, p4, p5, p6;

				p1 = new Vec3D(); p2 = new Vec3D(); p3 = new Vec3D();
				p4 = new Vec3D(); p5 = new Vec3D(); p6 = new Vec3D();
						
				int j = startIndex;
				
				p1.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
				p2.set(nextPath.get(j).x,nextPath.get(j).y,nextPath.get(j).z);
				p3.set(nextPath.get(j+1).x,nextPath.get(j+1).y,nextPath.get(j+1).z);			

				p4.set(nextPath.get(j+1).x,nextPath.get(j+1).y,nextPath.get(j+1).z); 
				p5.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
				p6.set(currentPath.get(j+1).x,currentPath.get(j+1).y,currentPath.get(j+1).z);
				
				this.addFace(p1, p2, p3);
				
				this.addFace(p4, p6, p5);
				
				/*
				System.out.println("p1:" + p1);
				System.out.println("p2:" + p2);
				System.out.println("p3:" + p3);
				*/
			}

			// add last face a bit manually: the last to the first to close the curve.
			// why? To avoid using % inside the above each loop, hopefully save some CPU cycles?
			// TODO - see if that's true

//			Vec3D prevProfileVert1 = currentPath.get(endIndex+1);
//			Vec3D prevProfileVert2 = currentPath.get(0);
//			Vec3D currProfileVert1 = nextPath.get(endIndex+1);
//			Vec3D currProfileVert2 = nextPath.get(0);
//
//			this.addFace(prevProfileVert1, prevProfileVert2, currProfileVert1);
//			this.addFace(prevProfileVert2, currProfileVert2, currProfileVert1);		

			
			Vec3D  p1, p2, p3, p4, p5, p6;

			p1 = new Vec3D(); p2 = new Vec3D(); p3 = new Vec3D();
			p4 = new Vec3D(); p5 = new Vec3D(); p6 = new Vec3D();
					
			int j = endIndex;
			
			p1.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
			p2.set(nextPath.get(j).x,nextPath.get(j).y,nextPath.get(j).z);
			p3.set(nextPath.get(0).x,nextPath.get(0).y,nextPath.get(0).z);			

			p4.set(nextPath.get(0).x,nextPath.get(0).y,nextPath.get(0).z); 
			p5.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
			p6.set(currentPath.get(0).x,currentPath.get(0).y,currentPath.get(0).z);
			
			this.addFace(p1, p2, p3);
			this.addFace(p4, p6, p5);			
			
			// next shape is now current
			currentPath = nextPath;

			// end for all profile shapes	
		}
		
		this.computeFaceNormals();
		this.setComputed(true);
		
	// end compute()
	}
	
	/*
	 * Given a 2D shape profile, fit it to the parallel transport frame at the given index
	 * 
	 */
	public ArrayList<Vec3D> shapeOnPath(LineStrip2D nextShapeVerts, ParallelTransportFrame frame, 
			int index) 
	{
		int numVerts = nextShapeVerts.getVertices().size();
		
		ArrayList<Vec3D> path = new ArrayList<Vec3D>( numVerts ); 
		
		float angle = 0;
		final float angleInc = MathUtils.TWO_PI / (numVerts);
		
		for (Vec2D currentVert : nextShapeVerts)
		{
			Vec3D p = currentVert.to3DXY();
			/*
			float m = currentVert.magnitude() * this.radius;
			
			float c = MathUtils.cos(angle) * m;
			float s = MathUtils.sin(angle) * m;
			*/
			
			Vec3D svert = frame.vertices.get(index);
			Vec3D sbinorm = frame.getBinormal(index);
			Vec3D snorm = frame.getNormal(index);
/*
			p.x = svert.x + c*sbinorm.x + s*snorm.x;
			p.y = svert.y + c*sbinorm.y + s*snorm.y;
			p.z = svert.z + c*sbinorm.z + s*snorm.z;
*/
			
			p.x = svert.x + currentVert.x*sbinorm.x + currentVert.y*snorm.x;
			p.y = svert.y + currentVert.x*sbinorm.y + currentVert.y*snorm.y;
			//p.z = svert.z + currentVert.x*sbinorm.z + currentVert.y*snorm.z;
			p.z = svert.z + currentVert.x*sbinorm.z + currentVert.y*snorm.z;
			
			
		/*
			p.x = svert.x + currentVert.x*sbinorm.x;
			p.y = svert.y + currentVert.x*sbinorm.y;
			p.z = svert.z + currentVert.x*sbinorm.z;			
		*/
			
			path.add(p);
			angle += angleInc;
		}
		return path;
	}


	List<Vec3D> getCircle(int i, float _radius) {
		int k = diameterQuality;
		List<Vec3D> vert;
		float theta = 0;
		float dt = MathUtils.TWO_PI/(k);
		
		if(i<this.circles.size()) {
			// circle exists, does not create a new one, just modify it
			vert = circles.get(i);
		} else {
			// new length, we have to allocate new objects
			vert = new ArrayList<Vec3D>(k+1);
			for(int j=0; j<=k; j++) 
				vert.add(new Vec3D());
			
		}
		
		for(int j=0; j<=k; j++) {
			float c = MathUtils.cos(theta) * _radius;
			float s = MathUtils.sin(theta) * _radius;

			Vec3D p = vert.get(j);
			p.x = soul.vertices.get(i).x + c*soul.getBinormal(i).x + s*soul.getNormal(i).x;
			p.y = soul.vertices.get(i).y + c*soul.getBinormal(i).y + s*soul.getNormal(i).y;
			p.z = soul.vertices.get(i).z + c*soul.getBinormal(i).z + s*soul.getNormal(i).z;

			theta += dt;
		}  

		// cache the result back
		circles.add(vert);
		
		return vert; 
	}
	

	void addCircles(List<Vec3D> circle1, List<Vec3D> circle2) {
		Vec3D  p1, p2, p3, p4, p5, p6;
		Face f1, f2;
		boolean must_add = false;
		
		for(int j=0; j<circle1.size()-1; j++) {

			try { // vertices exists, does not create new ones, just modify them
				
				f1 = this.faces.get(num_faces++);
				p1 = f1.a; p2 = f1.b; p3 = f1.c;
				
				f2 = this.faces.get(num_faces++);
				p4 = f2.a; p5 = f2.b; p6 = f2.c;
				
			} catch (IndexOutOfBoundsException e) { // new length, we have to allocate new objects
				
				//System.out.println("addCircles > new");
				
				p1 = new Vec3D(); p2 = new Vec3D(); p3 = new Vec3D();
				p4 = new Vec3D(); p5 = new Vec3D(); p6 = new Vec3D();
				
				must_add = true;
			}
			
			p1.set(circle1.get(j).x,circle1.get(j).y,circle1.get(j).z);       
			p2.set(circle2.get(j).x,circle2.get(j).y,circle2.get(j).z);
			p3.set(circle2.get(j+1).x,circle2.get(j+1).y,circle2.get(j+1).z);			

			p4.set(circle2.get(j+1).x,circle2.get(j+1).y,circle2.get(j+1).z); 
			p5.set(circle1.get(j).x,circle1.get(j).y,circle1.get(j).z);       
			p6.set(circle1.get(j+1).x,circle1.get(j+1).y,circle1.get(j+1).z);

			if(must_add) {
				this.addFace(p1, p2, p3);
				this.addFace(p4, p5, p6);
			}
		}
	}

	
	public void setCachedRadius(float[] c) {
		this.cachedRadius = c;
		if(c!=null) setUsedCachedRadius(true);
		else setUsedCachedRadius(false);
	}

	public void setUsedCachedRadius(boolean usedCachedRadius) {
		this.usedCachedRadius = usedCachedRadius;
	}

	public boolean isUsedCachedRadius() {
		return usedCachedRadius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public float getRadius() {
		return radius;
	}
	
	public int getDiameterQuality() {
		return diameterQuality;
	}

	public void setDiameterQuality(int diameterQuality) {
		this.diameterQuality = diameterQuality;
	}

	public int getCurveLength() {
		return curveLength;
	}

	public void setCurveLength(int curveLength) {
		this.curveLength = curveLength;
	}

	public List<List<Vec3D>> getCircles() {
		return circles;
	}


	public boolean isComputed() {
		return computed;
	}


	private void setComputed(boolean computed) {
		this.computed = computed;
	}

	
}
