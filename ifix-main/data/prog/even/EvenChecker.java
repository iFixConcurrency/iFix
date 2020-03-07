package even;

public class EvenChecker implements Runnable{

	private EvenGenerator generator;
	
	public EvenChecker(EvenGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run() {
		int nextValue;
		int i = 0;
		while(i < 100){
			i++;
			nextValue = generator.next();
			if(nextValue % 2 != 0){
				throw new RuntimeException("bug found");
			}
		}
		
	}

	
}
