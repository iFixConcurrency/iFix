package reorder1;

/*
 * Created Apr 27, 2005
 * @author Xuan
 */
public class SetThread extends Thread {
    SetCheck sc;
    public SetThread(SetCheck sc) {
	this.sc=sc;
    }
    public void run() {
	sc.set();
    }
}
