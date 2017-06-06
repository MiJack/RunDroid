package com.mijack;

/**
 * @author Mr.Yuan
 * @since 2017/1/23.
 */
@Deprecated
public class Counter implements Cloneable {

	private int length;
	private int[] datas;
	private static final int INIT_SIZE = 16;

	public Counter(int... ids) {
		length = ids == null ? 0 : ids.length;
		int initSize = INIT_SIZE;
		while (length >= initSize) {
			initSize *= 2;
		}
		datas = new int[initSize];
		if (length == 0) {
			length = 1;
			for (int i = 0; i < initSize; i++) {
				datas[i] = i < length ? 0 : -1;
			}
		} else {
			for (int i = 0; i < initSize; i++) {
				datas[i] = i < length ? ids[i] : -1;
			}
		}
	}

	public Counter(int[] datas, int length) {
		this.length = length;
		int initSize = INIT_SIZE;
		while (length >= initSize) {
			initSize *= 2;
		}
		this.datas = new int[initSize];
		if (length == 0) {
			length = 1;
			for (int i = 0; i < initSize; i++) {
				this.datas[i] = i < length ? 0 : -1;
			}
		} else {
			for (int i = 0; i < initSize; i++) {
				this.datas[i] = i < length ? datas[i] : -1;
			}
		}
	}

	public String getIdString() {
		if (length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(datas[0]);
		for (int i = 1; i < length; i++) {
			sb.append(".");
			sb.append(datas[i]);
		}
		return sb.toString();
	}

	@Override
	public Counter clone() {
		return new Counter(this.datas, this.length);
	}

	public void increment() {
		datas[length - 1] = datas[length - 1] + 1;
	}

	public void decrement() {
		datas[length - 1] = datas[length - 1] - 1;
	}

	public Counter append() {
		Counter clone = clone();
		//判断有没有越界的可能性
		if (length + 1 >= datas.length) {
			int[] newArray = new int[clone.datas.length * 2];
			System.arraycopy(clone.datas, 0, newArray, 0, clone.datas.length);
			clone.datas = newArray;
		}
		clone.datas[length] = 0;
		clone.length++;
		return clone;
	}
}