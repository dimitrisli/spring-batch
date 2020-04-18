/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.item;

/**
 * A base exception class that all exceptions thrown from an {@link ItemWriter} extend.
 * 
 * @author Ben Hale
 */
@SuppressWarnings("serial")
public abstract class ItemWriterException extends RuntimeException {

	/**
	 * Create a new {@link ItemWriterException} based on a message and another exception.
	 * 
	 * @param message the message for this exception
	 * @param cause the other exception
	 */
	public ItemWriterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new {@link ItemWriterException} based on a message.
	 * 
	 * @param message the message for this exception
	 */
	public ItemWriterException(String message) {
		super(message);
	}

}
