/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.vnfm.generic.utils;

import java.util.HashMap;
import java.util.Map;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvMapUtils {

  private static final Logger log = LoggerFactory.getLogger(EnvMapUtils.class);

  public static Map<String, String> createForLifeCycleEventExecutionOnVNFCInstance(
      VNFCInstance vnfcInstance) {
    Map<String, String> tempEnv = new HashMap<>();
    tempEnv = putOwnIpAndFloatingIp(tempEnv, vnfcInstance);
    tempEnv = putVNFCInstanceHostnameInMap(tempEnv, vnfcInstance);
    tempEnv = modifyUnsafeEnvVarNames(tempEnv);
    return tempEnv;
  }

  public static Map<String, String> createConfigurationParametersAndProvidesMapFromVNFR(
      VirtualNetworkFunctionRecord vnfr) {
    Map<String, String> res = new HashMap<>();

    if (vnfr.getProvides() == null
        || vnfr.getProvides().getConfigurationParameters() == null
        || vnfr.getConfigurations() == null
        || vnfr.getConfigurations().getConfigurationParameters() == null) return res;
    for (ConfigurationParameter configurationParameter :
        vnfr.getProvides().getConfigurationParameters()) {
      res.put(configurationParameter.getConfKey(), configurationParameter.getValue());
    }
    for (ConfigurationParameter configurationParameter :
        vnfr.getConfigurations().getConfigurationParameters()) {
      res.put(configurationParameter.getConfKey(), configurationParameter.getValue());
    }
    res = modifyUnsafeEnvVarNames(res);
    return res;
  }

  public static Map<String, String> modifyUnsafeEnvVarNames(Map<String, String> env) {

    Map<String, String> result = new HashMap<>();

    for (Map.Entry<String, String> entry : env.entrySet()) {
      result.put(entry.getKey().replaceAll("[^A-Za-z0-9_]", "_"), entry.getValue());
    }

    return result;
  }

  private static Map<String, String> putVNFCInstanceHostnameInMap(
      Map<String, String> map, VNFCInstance vnfcInstance) {
    map.put("hostname", vnfcInstance.getHostname());
    return map;
  }

  public static Map<String, String> clearVNFCInstanceHostnameInMap(Map<String, String> map) {
    map.remove("hostname");
    return map;
  }

  public static Map<String, String> clearEnvFromTempValues(
      Map<String, String> env, Map<String, String> tempEnv) {
    for (String key : tempEnv.keySet()) {
      env.remove(key);
    }
    return env;
  }

  private static Map<String, String> putOwnIpAndFloatingIp(
      Map<String, String> map, VNFCInstance vnfcInstance) {
    //Adding own ips
    if (vnfcInstance.getIps() != null) {
      for (Ip ip : vnfcInstance.getIps()) {
        log.debug("Adding net: " + ip.getNetName() + " with value: " + ip.getIp());
        map.put(ip.getNetName(), ip.getIp());
      }
    }

    //Adding own floating ip
    if (vnfcInstance.getFloatingIps() != null) {
      for (Ip fip : vnfcInstance.getFloatingIps()) {
        log.debug("adding floatingIp: " + fip.getNetName() + " = " + fip.getIp());
        map.put(fip.getNetName() + "_floatingIp", fip.getIp());
      }
    }
    return map;
  }

  public static Map<String, String> clearOwnIpAndFloatingIpInEnv(
      Map<String, String> env, VNFCInstance vnfcInstance) {
    //Clearing own ips
    for (Ip ip : vnfcInstance.getIps()) {
      env.remove(ip.getNetName());
    }
    //Clearing own floating ip
    for (Ip fip : vnfcInstance.getFloatingIps()) {
      env.remove(fip.getNetName() + "_floatingIp");
    }
    return env;
  }
}
