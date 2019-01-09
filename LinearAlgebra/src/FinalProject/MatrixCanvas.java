package FinalProject;

import Jama.Matrix;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class MatrixCanvas extends AnimatedCanvas {
	Matrix m;
	boolean preferredChannel = false;
	int channel = -1;
	public MatrixCanvas(Matrix m) {
		this.m = m;
	}
	public MatrixCanvas() {
		m = null;
	}

	public void setMatrix(Matrix x) {
		m = x;
	}
	public Matrix getMatrix() {
		if (m != null)
			return m;
		return null;
	}
	
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	

	@Override
	public void drawData() {
		if(m==null)return;
		double recW = getWidth() / m.getColumnDimension();
		double recH = getHeight() / m.getRowDimension();
		
		if(recW>recH) {
			recW=recH;
		}else recH = recW;
		if(recH == 0)
			recH = recW = 1;
		GraphicsContext g = getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		double offsety = (getHeight()/2.0 - recH*m.getRowDimension()/2.0);
		double offsetx = (getWidth()/2.0-recW*m.getColumnDimension()/2.0);
		//System.out.printf("%s,%s%n",offsetx,offsety)
		g.translate(offsetx, offsety);
		
		for (int i = 0; i < m.getArray().length; i++) {
			for (int j = 0; j < m.getArray()[0].length; j++) {
				int argb = (int) m.get(i,j);
				int red = (argb>>16)&0xFF;
				int green = (argb>>8)&0xFF;
				int blue = (argb>>0)&0xFF;
				g.setFill(Color.rgb(red, green, blue));
				g.fillRect((int)(i * (int)recW), (int)(j * (int)recH), (int)recW, (int)recH);
			}
		}
		g.translate(-offsetx, -offsety);
	}
	public static int checkRange(int i) {
		if(i>=0 && i<=255) {
			return i;
		}
		if(i<0)
			return 0;
		return 255;
	}

}
