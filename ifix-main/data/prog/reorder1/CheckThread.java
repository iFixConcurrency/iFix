package reorder1;

/*
 * Created Apr 27, 2005
 * @author Xuan
 * Removed the try-catch block so assertion works properly
 */
public class CheckThread extends Thread {
    SetCheck sc;
    public CheckThread(SetCheck sc) {
	this.sc=sc;
    }
    public void run() {
	boolean rst=sc.check();
	if (rst!= true)
		throw new RuntimeException("bug found");
	//assert (rst==true);
    }
}
