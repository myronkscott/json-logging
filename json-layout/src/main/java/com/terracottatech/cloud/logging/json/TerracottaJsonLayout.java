/*
 * Copyright IBM Corp. 2024, 2025
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
package com.terracottatech.cloud.logging.json;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TerracottaJsonLayout extends JsonLayout {

  private String file;
  private String product;

  public TerracottaJsonLayout() {
    // 1 log line per line
    setAppendLineSeparator(true);
    setJsonFormatter(new TerracottaJsonFormatter());
  }

  @Override
  public void start() {
    validate();
    super.start();
  }

  private void validate() {
    Objects.requireNonNull(getFile());
    Objects.requireNonNull(getProduct());

    // If TZ not set
    if (getTimestampFormatTimezoneId() == null) {
      if (Boolean.getBoolean("terracotta.cloud.logging.utc") || "true".equals(System.getenv("JSON_LOGGING_UTC"))) {
        setTimestampFormatTimezoneId("UTC");
      } else {
        setTimestampFormatTimezoneId(ZoneId.systemDefault().getId());
      }
    }

    // If format not set
    if (getTimestampFormat() == null) {
      // ISO_OFFSET_DATE_TIME
      setTimestampFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }
  }

  @Override
  protected void addCustomDataToJsonMap(Map<String, Object> map, ILoggingEvent logEvent) {
    if (!logEvent.getMarkerList().isEmpty()) {
      // only log top marker, no child
      map.put(Key.MARKER, logEvent.getMarkerList().get(0).getName());
    }

    String accid = System.getProperty("terracotta.cloud.logging.accid");
    if (accid != null) {
      map.put(Key.ACCID, accid);
    }

    String envname = System.getProperty("terracotta.cloud.logging.envname");
    if (envname != null) {
      map.put(Key.ENVNAME, envname);
    }

    map.put(Key.FILE, getFile());

    Map<String, String> ctx = new HashMap<>();

    ctx.put(Key.PRODUCT, getProduct());

    String node = System.getProperty("terracotta.cloud.logging.node");
    if (node != null) {
      ctx.put(Key.NODE, node);
    }

    String stripe = System.getProperty("terracotta.cloud.logging.stripe");
    if (stripe != null) {
      ctx.put(Key.STRIPE, stripe);
    }

    String cluster = System.getProperty("terracotta.cloud.logging.cluster");
    if (cluster != null) {
      ctx.put(Key.CLUSTER, cluster);
    }
    map.put(Key.ID, ctx);
  }

  private String getFile() {
    return file;
  }

  public void setFile(final String file) {
    this.file = file;
  }

  private String getProduct() {
    return product;
  }

  public void setProduct(final String product) {
    this.product = product;
  }
}
