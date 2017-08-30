import org.bson.Document;

public class DocumentHashExperiments {

	public static void main(String[] args) {
		Document d1 = new Document("a",1);
		Document e1 = new Document("e.1","hallo");
		e1.append("e.2", 100);
		d1.put("b", "c");
		d1.put("e", e1);

		Document d2 = new Document("a",1);
		Document e2 = new Document("e.1","hallo");
		e2.append("e.2", 99);
		d2.put("e", e2);
		d2.put("b", "c");
		
		
		System.out.println("d1: "+d1.hashCode()+"     d2: "+d2.hashCode());
	}

}
