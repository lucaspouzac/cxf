/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.rs.security.jose.jwe;

import java.security.interfaces.ECPrivateKey;

import org.apache.cxf.rs.security.jose.JoseUtils;
import org.apache.cxf.rs.security.jose.jwa.Algorithm;
import org.apache.cxf.rs.security.jose.jwk.JsonWebKey;
import org.apache.cxf.rs.security.jose.jwk.JwkUtils;


public class EcdhDirectKeyJweDecryption extends DirectKeyJweDecryption {
    public EcdhDirectKeyJweDecryption(ECPrivateKey privateKey, String supportedCtAlgo) {
        super(null, 
              new EcdhDirectKeyDecryptionAlgorithm(privateKey), 
              new AesGcmContentDecryptionAlgorithm(supportedCtAlgo));
    }
    protected static byte[] getDecryptedContentEncryptionKeyFromHeaders(JweHeaders headers,
                                                                        ECPrivateKey privateKey) {
        Algorithm jwtAlgo = Algorithm.valueOf(headers.getContentEncryptionAlgorithm());
        JsonWebKey publicJwk = headers.getJsonWebKey("epv");
        String apuHeader = (String)headers.getHeader("apu");
        byte[] apuBytes = apuHeader == null ? null : JoseUtils.decode(apuHeader);
        String apvHeader = (String)headers.getHeader("apv");
        byte[] apvBytes = apvHeader == null ? null : JoseUtils.decode(apvHeader);
        return JweUtils.getECDHKey(privateKey, JwkUtils.toECPublicKey(publicJwk), 
                                   apuBytes, apvBytes, jwtAlgo.getJwtName(), jwtAlgo.getKeySizeBits());
    }
    protected static class EcdhDirectKeyDecryptionAlgorithm extends DirectKeyDecryptionAlgorithm {
        private ECPrivateKey privateKey;
        public EcdhDirectKeyDecryptionAlgorithm(ECPrivateKey privateKey) {    
            super((byte[])null);
            this.privateKey = privateKey;
        }
        @Override
        public byte[] getDecryptedContentEncryptionKey(JweCompactConsumer consumer) {
            super.validateKeyEncryptionKey(consumer);
            
            return getDecryptedContentEncryptionKeyFromHeaders(consumer.getJweHeaders(), privateKey);
        }
    }
}
