/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package br.com.caelum.vraptor.converter.l10n;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;

import br.com.caelum.vraptor.Convert;
import br.com.caelum.vraptor.Converter;
import br.com.caelum.vraptor.converter.ConversionError;
import br.com.caelum.vraptor.converter.JstlWrapper;
import br.com.caelum.vraptor.core.RequestInfo;
import br.com.caelum.vraptor.ioc.RequestScoped;

/**
 * Localized version of VRaptor's Double converter. This component is optional and must be declared in web.xml before
 * using. If the input value if empty or a null string, null values are returned. If the input string is not a number a
 * {@link ConversionError} will be throw.
 * 
 * @author Otávio Scherer Garcia
 * @since 3.1.2
 */
@Convert(Double.class)
@RequestScoped
public class LocaleBasedDoubleConverter
    implements Converter<Double> {

    private final JstlWrapper jstlWrapper = new JstlWrapper();
    private final RequestInfo request;

    public LocaleBasedDoubleConverter(RequestInfo request) {
        this.request = request;
    }

    public Double convert(String value, Class<? extends Double> type, ResourceBundle bundle) {
        // skip null of empty values
        if (value == null || value.trim().length() == 0) {
            return null;
        }

        try {
            final Locale locale = jstlWrapper.findLocale(request);
            DecimalFormat fmt = new DecimalFormat("##0,00", new DecimalFormatSymbols(locale));

            // DecimalFormat.parse can return long values, so it's more securely call floatValue
            return fmt.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new ConversionError(MessageFormat.format(bundle.getString("is_not_a_valid_number"), value));
        }
    }

}
