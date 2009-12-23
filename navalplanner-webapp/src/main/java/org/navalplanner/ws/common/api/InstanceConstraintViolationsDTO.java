/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.ws.common.api;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * DTO for modeling the list of constraint violations on a given instance.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class InstanceConstraintViolationsDTO {

    @XmlAttribute(name="instance-id")
    public String instanceId;

    @XmlElement(name="constraint-violation")
    public List<ConstraintViolationDTO> constraintViolations;

    public InstanceConstraintViolationsDTO() {}

    public InstanceConstraintViolationsDTO(String instanceId,
        List<ConstraintViolationDTO> constraintViolations) {

        this.instanceId = instanceId;
        this.constraintViolations = constraintViolations;

    }

    public static InstanceConstraintViolationsDTO create(String instanceId,
        String message) {

        List<ConstraintViolationDTO> constraintViolations =
            new ArrayList<ConstraintViolationDTO>();

        constraintViolations.add(new ConstraintViolationDTO(null, message));

        return new InstanceConstraintViolationsDTO(instanceId,
            constraintViolations);

    }

}
