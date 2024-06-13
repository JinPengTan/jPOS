/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2023 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.log;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jpos.log.evt.*;

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "t"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = Start.class, name = "start"),
  @JsonSubTypes.Type(value = Stop.class, name = "stop"),
  @JsonSubTypes.Type(value = Deploy.class, name = "deploy"),
  @JsonSubTypes.Type(value = UnDeploy.class, name = "undeploy"),
  @JsonSubTypes.Type(value = LogMessage.class, name = "msg")
})

public sealed interface AuditLogEvent permits LogMessage, Deploy, UnDeploy, Start, Stop { }