package com.mijack;

/**
 * Created by Mr.Yuan on 2017/2/7.
 */
public class TestDemo {


	public void fun7() {
		if (System.currentTimeMillis() % 2 == 1) {
			System.out.println("if");
		} else {
			System.out.println("then");
		}
		if (System.currentTimeMillis() % 2 == 1) {
			System.out.println("if");
		} else {
			System.out.println("then");
		}
	}

	public void fun8() {
		if (System.currentTimeMillis() % 2 == 1) {
			System.out.println("if");
			if (System.currentTimeMillis() % 2 == 1) {
				System.out.println("if");
			} else {
				System.out.println("then");
			}
		} else {
			System.out.println("then");
		}
	}
}
