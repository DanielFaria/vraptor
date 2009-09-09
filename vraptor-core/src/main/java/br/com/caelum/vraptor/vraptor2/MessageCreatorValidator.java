/***
 *
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.vraptor.vraptor2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.vraptor.i18n.FixedMessage;
import org.vraptor.validator.ValidationErrors;

import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.MethodInfo;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.proxy.MethodInvocation;
import br.com.caelum.vraptor.proxy.Proxifier;
import br.com.caelum.vraptor.proxy.SuperMethod;
import br.com.caelum.vraptor.resource.ResourceMethod;
import br.com.caelum.vraptor.validator.Message;
import br.com.caelum.vraptor.validator.ValidationError;
import br.com.caelum.vraptor.validator.Validations;
import br.com.caelum.vraptor.view.ResultException;
import br.com.caelum.vraptor.view.Results;

/**
 * The vraptor2 compatible messages creator.
 *
 * @author Guilherme Silveira
 */
@RequestScoped
public class MessageCreatorValidator implements Validator {

    private final Proxifier proxifier;
    private final Result result;
    private final ValidationErrors errors;

    private Object[] argsToUse;

    private Method method;
    private Class<?> typeToUse;
	private final ResourceMethod resource;
	private final HttpServletRequest request;
	private final MethodInfo info;
	private boolean containsErrors;

    public MessageCreatorValidator(Proxifier proxifier, Result result, ValidationErrors errors,
    		HttpServletRequest request, MethodInfo info) {
        this.proxifier = proxifier;
        this.result = result;
        this.errors = errors;
		this.resource = info.getResourceMethod();
		this.request = request;
		this.info = info;
    }

    public void checking(Validations validations) {
        List<Message> messages = validations.getErrors();
            for (Message s : messages) {
            	add(s);
            }
            validate();
    }

    public Validator onError() {
        return this;
    }

    public <T> T goTo(Class<T> type) {
        this.typeToUse = type;
        return proxifier.proxify(type, new MethodInvocation<T>() {
            public Object intercept(T proxy, Method method, Object[] args, SuperMethod superMethod) {
                if (MessageCreatorValidator.this.method == null) {
                    MessageCreatorValidator.this.argsToUse = args;
                    MessageCreatorValidator.this.method = method;
                }
                return null;
            }
        });
    }

	public void add(Message message) {
		containsErrors = true;
        this.errors.add(new FixedMessage(message.getCategory(), message.getMessage(), message.getCategory()));
	}

	public void validate() {
        if (containsErrors) {
            result.include("errors", this.errors);
            if (method != null) {
                Object instance = result.use(Results.logic()).forwardTo(typeToUse);
                try {
                    method.invoke(instance, argsToUse);
                } catch (Exception e) {
                    throw new ResultException(e);
                }
            } else {
            	if(Info.isOldComponent(resource.getResource())) {
            		info.setResult("invalid");
            		result.use(Results.page()).forward();
            	} else {
                	result.use(Results.page()).forward(request.getRequestURI());
            	}
            }
            // finished just fine
            throw new ValidationError(new ArrayList<Message>());
        }
	}

	public void add(Collection<? extends Message> messages) {
		for (Message message : messages) {
			this.add(message);
		}
	}

	public boolean hasErrors() {
		return containsErrors;
	}

}
