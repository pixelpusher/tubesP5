/**
 * 
 */
package tubesP5.library;

import java.util.ArrayList;
import java.util.Iterator;
import toxi.geom.LineStrip2D;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import toxi.math.MathUtils;

/**
 * @author evan
 *
 */
public final class OrganicProfileTube extends Tube {

	// shape profiles to map across the path
	public ArrayList<LineStrip2D> profiles;
	
	// shape on path strategies
	final public static byte MIX_FRAMES = 0;
	final public static byte TANGENT_FRAMES = 1;
	final public static byte PERP_FRAMES = 2;

	private static final byte BASKET_FRAMES = 3;
	
	private byte strategy; 
	
	
	/**
	 * @param soul
	 */
	public OrganicProfileTube(ParallelTransportFrame soul) {
		super(soul);
		this.profiles = new ArrayList<LineStrip2D>();
		this.strategy = OrganicProfileTube.TANGENT_FRAMES;
	}


	public OrganicProfileTube setStrategy(byte strategy)
	{
		this.strategy = strategy;
		
		return this;
	}
	
	public byte getStrategy()
	{
		return this.strategy;
	}
	
	
	/**
	 * @return the profiles
	 */
	public ArrayList<LineStrip2D> getProfiles() {
		return profiles;
	}


	/**
	 * @param profiles the profiles to copy into this one
	 */
	public OrganicProfileTube setProfiles(ArrayList<LineStrip2D> profiles) {
		this.profiles.clear(); 
		this.profiles.ensureCapacity(profiles.size());
		for (LineStrip2D profile : profiles)
		{
			this.profiles.add( profile );
		}
		
		
		return this;
	}
	
	
	// TODO - change exception to a proper type
	
	/* (non-Javadoc)
	 * @see tubesP5.library.Tube#compute()
	 */
	@Override
	public void compute() //throws Exception
	{
		// size check... maybe this should change to something else...
		if (this.profiles.size() < 3)
		{
			System.out.println("WARNING:: OrganicProfileTube:compute() - profile has less than 3 elements");
			return;
		}
		
		Vec3D  p1, p2, p3, p4;

		p1 = new Vec3D(); p2 = new Vec3D(); p3 = new Vec3D();
		p4 = new Vec3D();

		
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
		
		Iterator<LineStrip2D> profilesIterator = this.profiles.iterator();

		LineStrip2D currentShapeVerts = profilesIterator.next();

		int i=0; // index

		ArrayList<Vec3D> currentPath = shapeOnPath(currentShapeVerts, soul, i);
		
		while (++i < soul.getCurveLength())
		{
			//System.out.println("profile pts:" + currentShapeVerts.size());
			//System.out.println("soul[" + i + "]=" +  soul.get(i));
			
			LineStrip2D nextShapeVerts = profilesIterator.next();
			ArrayList<Vec3D> nextPath = shapeOnPath(nextShapeVerts, soul, i);

			// blindly try... so what if we hit an exception... catch it later


			int endIndex = currentPath.size()-2;
			
			for (int startIndex = 0; startIndex < endIndex; startIndex++  )
			{
				int j = startIndex;
				
				p1.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
				p2.set(nextPath.get(j).x,nextPath.get(j).y,nextPath.get(j).z);
				p3.set(nextPath.get(j+1).x,nextPath.get(j+1).y,nextPath.get(j+1).z);			
				p4.set(currentPath.get(j+1).x,currentPath.get(j+1).y,currentPath.get(j+1).z);
				
				this.addFace(p1, p3, p2);
				this.addFace(p1, p4, p3);

			}

			// add last face a bit manually: the last to the first to close the curve.
			// why? To avoid using % inside the above each loop, hopefully save some CPU cycles?
			// TODO - test if that's true
							
			int j = endIndex+1;
			
			p1.set(currentPath.get(j).x,currentPath.get(j).y,currentPath.get(j).z);       
			p2.set(nextPath.get(j).x,nextPath.get(j).y,nextPath.get(j).z);
			p3.set(nextPath.get(0).x,nextPath.get(0).y,nextPath.get(0).z);			
			p4.set(currentPath.get(0).x,currentPath.get(0).y,currentPath.get(0).z);
			
			this.addFace(p1, p3, p2);
			this.addFace(p1, p4, p3);			
			
			
			// next shape is now current
			currentPath = nextPath;

			
		} // end for all profile shapes	
		
		// TODO -- end cap and start cap
		// add end cap
		// find centroid of current shape
		// for each point, until last point, add triangle for current point, centroid, next point.
		// add triangle for last point, centroid, first point
		
		
		this.computeFaceNormals();
		this.setComputed(true);
		
	// end compute()
	}
	
	/*
	 * Given a 2D shape profile, fit it to the parallel transport frame at the given vertex index
	 * 
	 */
	public ArrayList<Vec3D> shapeOnPath(LineStrip2D nextShapeVerts, ParallelTransportFrame frame, 
			int index) 
	{
		int numVerts = nextShapeVerts.getVertices().size();
		
		ArrayList<Vec3D> path = new ArrayList<Vec3D>( numVerts ); 
		
		float angle = 0;
		final float angleInc = MathUtils.TWO_PI / numVerts;
		
		
		for (Vec2D currentVert : nextShapeVerts)
		{
			Vec3D p = currentVert.to3DXY();
			/*
			float m = currentVert.magnitude() * this.radius;
			
			float c = MathUtils.cos(angle) * m;
			float s = MathUtils.sin(angle) * m;
			*/
			
			Vec3D svert = frame.vertices.get(index);
			
/*
			p.x = svert.x + c*sbinorm.x + s*snorm.x;
			p.y = svert.y + c*sbinorm.y + s*snorm.y;
			p.z = svert.z + c*sbinorm.z + s*snorm.z;
*/
			switch(this.strategy)
			{
			
				case OrganicProfileTube.MIX_FRAMES:
				{
					Vec3D sbinorm = frame.getBinormal(index);
					Vec3D snorm = frame.getNormal(index);
					
					p.x = svert.x + currentVert.x*sbinorm.x + currentVert.y*snorm.x;
					p.y = svert.y + currentVert.x*sbinorm.y + currentVert.y*snorm.y;
					p.z = svert.z + currentVert.x*sbinorm.z + currentVert.y*snorm.z;
				}
				break;
				
				case OrganicProfileTube.TANGENT_FRAMES:
				{
					Vec3D stan = frame.tangents.get(index);
					
					float mag = currentVert.magnitude();
					
					//p.x = svert.x + currentVert.x*stan.x + currentVert.y*stan.x;
					//p.y = svert.y + currentVert.x*stan.y + currentVert.y*stan.y;
					
					p.x = svert.x + mag*stan.x;
					p.y = svert.y + mag*stan.y;
					//p.z = svert.z + currentVert.y*stan.z;
					//p.z = svert.z + MathUtils.sin(angle)*currentVert.y + MathUtils.cos(angle)*currentVert.x; // extrude at 90;
					p.z = svert.z + MathUtils.sin(angle)*MathUtils.abs(currentVert.y) + 
							MathUtils.cos(angle)*MathUtils.abs(currentVert.y); // extrude at 90;
				}
				break;
				
				case OrganicProfileTube.PERP_FRAMES:
				{
					//Vec3D stan = frame.tangents.get(index);
					
					float mag = currentVert.magnitude();
					
					Vec3D svertDir = new Vec3D(svert);
					svertDir.z = 0f;
					svertDir.normalizeTo(mag);
					
					//p.x = svert.x + currentVert.x*stan.x + currentVert.y*stan.x;
					//p.y = svert.y + currentVert.x*stan.y + currentVert.y*stan.y;
					
					p.x = svert.x + svertDir.x;
					p.y = svert.y + svertDir.y;
					
					// totally fucked up with missing geom, but stunning...
					//p.z = svert.z + MathUtils.sin(angle)*mag + MathUtils.cos(angle)*mag; // extrude at 90
					
					// standard profile wrapped to a circular path
					p.z = svert.z + MathUtils.sin(angle)*MathUtils.abs(currentVert.y) + 
							MathUtils.cos(angle)*MathUtils.abs(currentVert.y); // extrude at 90;
					
					/*
					float s = MathUtils.sin(angle);
					float c = MathUtils.cos(angle);
					
					float r = 2f;
					
					// needs  ramp up, but them connect in the end
					p.z = svert.z +  c*2*r*angle/MathUtils.TWO_PI;
					*/
					
					
					/*
					s += 1f;
					s*= 0.5;
					s += 0.5f; 
					if (angle >= MathUtils.PI)
					{
						mag = currentVert.y;
						
						//mag *= stan.z;
						
						// strangely round...
						//p.z = svert.z + (s*s - 0.5f)*mag;
						p.z = svert.z + s*mag;
						
					}
					else
					{
						float a = 1f - angle/MathUtils.TWO_PI;
						
						
						p.z = svert.z + a*a*20f + 2f;
					}
					*/
					
					
				}
				break;
				
				
				case OrganicProfileTube.BASKET_FRAMES:
				{
					//Vec3D stan = frame.tangents.get(index);
					
					float mag = currentVert.magnitude();
					
					Vec3D svertDir = svert.copy().normalizeTo(mag); // normalize in direction of point from
					// center of spiral
					
					p.x = svert.x + svertDir.x;
					p.y = svert.y + svertDir.y;
					
					// this one does rounded
					//p.z = svert.z + MathUtils.sin(angle)*mag + MathUtils.cos(angle)*mag; // extrude at 90
					
					// this is half-circle
					p.z = svert.z + MathUtils.sin(angle)*mag; // extrude at 90
					
				}
				break;
			}
			
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
		
		

}
