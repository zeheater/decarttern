package MyApp;

import com.google.gson.*;

public class App {

	public static void main(String[] args) {
		JsonArray cartObj = new JsonArray();
		JsonObject entry = new JsonObject();
		entry.addProperty("itemName", "A");
		entry.addProperty("price", 1000.0f);
		cartObj.add(entry.deepCopy());
		entry.addProperty("itemName", "B");
		entry.addProperty("price", 2000.0f);
		cartObj.add(entry.deepCopy());
		entry.addProperty("itemName", "C");
		entry.addProperty("price", 3000.0f);
		cartObj.add(entry.deepCopy());
		entry.addProperty("itemName", "D");
		entry.addProperty("price", 4000.0f);
		cartObj.add(entry.deepCopy());
		entry.addProperty("itemName", "E");
		entry.addProperty("price", 5000.0f);
		cartObj.add(entry.deepCopy());

		//NormalCheckout nc = new NormalCheckout();
		//nc.calculate(cartObj);
		//nc.print();
		//TaxCheckout tc = new TaxCheckout();
		//tc.calculate(cartObj);
		//tc.print();
		ConditionalTaxCheckout ctc = new ConditionalTaxCheckout(true);
		ctc.calculate(cartObj);
		ctc.print();
		ctc.printItems();

		ConditionalTaxCheckout ctc2 = new ConditionalTaxCheckout(false);
		ctc2.calculate(cartObj);
		ctc2.print();
		ctc2.printItems();

		BuyXGetY bxgy = new BuyXGetY(true);
		bxgy.calculate(cartObj);
		bxgy.print();
		bxgy.printItems();

		//System.out.println(new com.google.gson.Gson().toJson(cartObj));
	}

	static interface Calculator {
		void calculate(JsonArray cartObject);
	}

	static abstract class BaseCheckout implements Calculator {

		@Override
		public void calculate(JsonArray cartObject) {}
	}

	static class NormalCheckout extends BaseCheckout {

		JsonObject CheckoutDetails = new JsonObject();
		protected JsonArray cartObject;

		@Override
		public void calculate(JsonArray cartObject) {
			
			this.cartObject = cartObject;
			float dpp = 0.0f;

			for (JsonElement el: cartObject) {
				dpp += el.getAsJsonObject().getAsJsonPrimitive("price").getAsFloat();
			}

			CheckoutDetails.addProperty("dppvalue", dpp);
			CheckoutDetails.addProperty("grandtotal", dpp);
		}

		protected void print() {
			System.out.println(new com.google.gson.Gson().toJson(CheckoutDetails));
		}

		protected void printItems() {
			System.out.println(new com.google.gson.Gson().toJson(cartObject));
		}
	}
	
	static class TaxCheckout extends NormalCheckout {
		
		float tax = 10.0f;
		float serviceTax = 3.0f;

		@Override
		public void calculate(JsonArray cartObject) {
			super.calculate(cartObject);
			CheckoutDetails.addProperty("tax", tax);
			CheckoutDetails.addProperty("serviceTax", serviceTax);
			float dpp = CheckoutDetails.getAsJsonPrimitive("dppvalue").getAsFloat();
			float grandtotal = ((dpp * (100.f + serviceTax) / 100.0f) * (100.0f + tax)) / 100.0f;
			CheckoutDetails.addProperty("grandtotal", grandtotal);
		}
	}

	static class ConditionalTaxCheckout extends TaxCheckout {

		boolean isDinein;

		public ConditionalTaxCheckout(boolean isDinein){
			this.isDinein = isDinein;
		}

		@Override
		public void calculate(JsonArray cartObject) {
			if (!isDinein) serviceTax = 0.0f;
			super.calculate(cartObject);
			CheckoutDetails.addProperty("tax", tax);
			float dpp = CheckoutDetails.getAsJsonPrimitive("dppvalue").getAsFloat();
			float servicetaxval = Math.round(dpp *  serviceTax / 100.0f);
			float taxval = Math.round((dpp+servicetaxval) * tax / 100.0f);
			float grandtotal = (dpp + servicetaxval + taxval);
			CheckoutDetails.addProperty("grandtotal", grandtotal);
		}
	}

	static class BuyXGetY extends ConditionalTaxCheckout {

		public BuyXGetY(boolean isDineIn) {
			super(isDineIn);
		}


		private void findXaddY(JsonArray cartObject) {
			JsonArray addYItems = new JsonArray();

			for (JsonElement el : cartObject) {
				if (el.getAsJsonObject().getAsJsonPrimitive("itemName").getAsString().equals("A")) {
					JsonObject yBonusItem = new JsonObject();
					yBonusItem.addProperty("itemName", "ZZZ");
					yBonusItem.addProperty("price", 0);
					yBonusItem.addProperty("note", "Free Y For X");
					addYItems.add(yBonusItem);
				}
			}

			cartObject.addAll(addYItems);
		}

		@Override
		public void calculate(JsonArray cartObject) {
			findXaddY(cartObject);
			super.calculate(cartObject);

		}
	}
}
