

public class Main {

    public static void main(String[] args) {
        try {

        	PageCache pc;
        	Page p;
        	// PageCache pc = new PageCache("db/tst");
        	// Page p = pc.getPage(1);
        	// for(byte i = 0; i < 100; i++)
        	// 	p.data[i] = i;
        	// p.write();
        	// pc.close();

        	pc = new PageCache("db/tst");
        	p = pc.getPage(1);
        	for(byte i = 0; i < 100; i++)
        		System.out.println(p.data[i]);
        	pc.close();

        } catch(Exception e) {
            System.out.println("Something goes wrong.");
        }
    }

}
