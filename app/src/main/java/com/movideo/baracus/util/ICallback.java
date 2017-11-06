/*******************************************************************************
 * Copyright © Movideo Pty Limited 2013. All Rights Reserved
 ******************************************************************************/
package com.movideo.baracus.util;

/**
 * Interface for callback classes
 *
 * @param <T> Type of result class which will be passed into execute method
 * @author rranawaka
 */
public interface ICallback<T>
{
	void onSuccess(T result);

	void onFailure(Throwable t);
}
