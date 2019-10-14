# Visitor Design Pattern in Java

#### 1. What is the visitor design pattern/

- Allows you to add methods to classes of different types without much altering to those classes
- Can make completely different methods depending on the class used
- Allows to define external classes that can extend other classes without majorly editing them. 

![GoF Design Patterns - Visitor](https://online.visual-paradigm.com/repository/images/8850fa2f-dba7-40e4-b06f-2e77004456af.png)



#### 2. Example

 VISITOR.JAVA

```JAVA
// The visitor pattern is used when you have to perform
// the same action on many objects of different types

interface Visitor {
	
	// Created to automatically use the right 
	// code based on the Object sent
	// Method Overloading
	
	public double visit(Liquor liquorItem);
	
	public double visit(Tobacco tobaccoItem);
	
	public double visit(Necessity necessityItem);
	
}
```

TAXVISITOR.JAVA

```JAVA
import java.text.DecimalFormat;

// Concrete Visitor Class

class TaxVisitor implements Visitor {
	
	// This formats the item prices to 2 decimal places
	
	DecimalFormat df = new DecimalFormat("#.##");

	// This is created so that each item is sent to the
	// right version of visit() which is required by the
	// Visitor interface and defined below
	
	public TaxVisitor() {
	}
	
	// Calculates total price based on this being taxed
	// as a liquor item
	
	public double visit(Liquor liquorItem) {
		System.out.println("Liquor Item: Price with Tax");
		return Double.parseDouble(df.format((liquorItem.getPrice() * .18) + liquorItem.getPrice()));
	}
	
	// Calculates total price based on this being taxed
	// as a tobacco item
		
	public double visit(Tobacco tobaccoItem) {
		System.out.println("Tobacco Item: Price with Tax");
		return Double.parseDouble(df.format((tobaccoItem.getPrice() * .32) + tobaccoItem.getPrice()));
	}
	
	// Calculates total price based on this being taxed
	// as a necessity item
		
	public double visit(Necessity necessityItem) {
		System.out.println("Necessity Item: Price with Tax");
		return Double.parseDouble(df.format(necessityItem.getPrice()));
	}

}
```

VISITABLE.JAVA

```JAVA
interface Visitable {

	// Allows the Visitor to pass the object so
	// the right operations occur on the right 
	// type of object.
	
	// accept() is passed the same visitor object
	// but then the method visit() is called using 
	// the visitor object. The right version of visit()
	// is called because of method overloading
	
	public double accept(Visitor visitor);
	
}
```

LIQUOR.JAVA

```JAVA
class Liquor implements Visitable {
	
	private double price;

	Liquor(double item) {
		price = item;
	}

	public double accept(Visitor visitor) {
		return visitor.visit(this);
	}

	public double getPrice() {
		return price;
	}
	
}
```

NECESSITY.JAVA

```JAVA
class Necessity implements Visitable {
	
	private double price;

	Necessity(double item) {
		price = item;
	}

	public double accept(Visitor visitor) {
		return visitor.visit(this);
	}

	public double getPrice() {
		return price;
	}
	
}
```

TOBACCO.JAVA

```JAVA
class Tobacco implements Visitable {
	
	private double price;

	Tobacco(double item) {
		price = item;
	}

	public double accept(Visitor visitor) {
		return visitor.visit(this);
	}

	public double getPrice() {
		return price;
	}
	
}
```

TAXHOLIDAYVISITOR.JAVA

```JAVA
import java.text.DecimalFormat;

// Concrete Visitor Class

class TaxHolidayVisitor implements Visitor {
	
	// This formats the item prices to 2 decimal places
	
	DecimalFormat df = new DecimalFormat("#.##");

	// This is created so that each item is sent to the
	// right version of visit() which is required by the
	// Visitor interface and defined below
	
	public TaxHolidayVisitor() {
	}
	
	// Calculates total price based on this being taxed
	// as a liquor item
	
	public double visit(Liquor liquorItem) {
		System.out.println("Liquor Item: Price with Tax");
		return Double.parseDouble(df.format((liquorItem.getPrice() * .10) + liquorItem.getPrice()));
	}
	
	// Calculates total price based on this being taxed
	// as a tobacco item
		
	public double visit(Tobacco tobaccoItem) {
		System.out.println("Tobacco Item: Price with Tax");
		return Double.parseDouble(df.format((tobaccoItem.getPrice() * .30) + tobaccoItem.getPrice()));
	}
	
	// Calculates total price based on this being taxed
	// as a necessity item
		
	public double visit(Necessity necessityItem) {
		System.out.println("Necessity Item: Price with Tax");
		return Double.parseDouble(df.format(necessityItem.getPrice()));
	}

}
```

VISITORTEST.JAVA

```JAVA
public class VisitorTest {
	public static void main(String[] args) {
		
		TaxVisitor taxCalc = new TaxVisitor();
		TaxHolidayVisitor taxHolidayCalc = new TaxHolidayVisitor();
		
		Necessity milk = new Necessity(3.47);
		Liquor vodka = new Liquor(11.99);
		Tobacco cigars = new Tobacco(19.99);
		
		System.out.println(milk.accept(taxCalc) + "\n");
		System.out.println(vodka.accept(taxCalc) + "\n");
		System.out.println(cigars.accept(taxCalc) + "\n");
		
		System.out.println("TAX HOLIDAY PRICES\n");

		System.out.println(milk.accept(taxHolidayCalc) + "\n");
		System.out.println(vodka.accept(taxHolidayCalc) + "\n");
		System.out.println(cigars.accept(taxHolidayCalc) + "\n");

	}
}
```

