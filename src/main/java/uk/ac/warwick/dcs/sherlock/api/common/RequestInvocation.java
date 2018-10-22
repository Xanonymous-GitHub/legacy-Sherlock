package uk.ac.warwick.dcs.sherlock.api.common;

import uk.ac.warwick.dcs.sherlock.api.util.Tuple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RequestInvocation extends Tuple<Method, Object> {

	private RequestInvocation(Method method, Object obj) {
		super(method, obj);
	}

	public static RequestInvocation of(Method method, Object obj) {
		return new RequestInvocation(method, obj);
	}

	public Request post(Request reference) {
		try {
			this.getKey().setAccessible(true);
			Object responce = this.getKey().invoke(this.getValue(), reference);
			if (responce instanceof Request) {
				return (Request) responce;
			}
			return null;
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void respond(Request reference) {
		try {
			this.getKey().setAccessible(true);
			this.getKey().invoke(this.getValue(), reference);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
