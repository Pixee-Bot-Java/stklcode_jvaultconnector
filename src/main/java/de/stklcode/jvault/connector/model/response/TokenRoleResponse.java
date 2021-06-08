/*
 * Copyright 2016-2021 Stefan Kalscheuer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stklcode.jvault.connector.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.stklcode.jvault.connector.exception.InvalidResponseException;
import de.stklcode.jvault.connector.model.TokenRole;
import de.stklcode.jvault.connector.model.response.embedded.TokenData;

import java.io.IOException;
import java.util.Map;

/**
 * Vault response from token role lookup providing Token information in {@link TokenData} field.
 *
 * @author Stefan Kalscheuer
 * @since 0.9
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TokenRoleResponse extends VaultDataResponse {
    private TokenRole data;

    /**
     * Set data. Parses response data map to {@link TokenRole}.
     *
     * @param data Raw response data
     * @throws InvalidResponseException on parsing errors
     */
    @Override
    public void setData(final Map<String, Object> data) throws InvalidResponseException {
        var mapper = new ObjectMapper();
        try {
            this.data = mapper.readValue(mapper.writeValueAsString(data), TokenRole.class);
        } catch (IOException e) {
            throw new InvalidResponseException("Failed deserializing response", e);
        }
    }

    /**
     * @return TokenRole data
     */
    public TokenRole getData() {
        return data;
    }
}
