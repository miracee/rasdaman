/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.server.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;

/**
 * This is an axis in geographic coordinates. See the WCPS standard.
 */
public class DomainElement implements Cloneable {

    //public static final String WGS84_CRS = "http://www.opengis.net/def/crs/EPSG/0/4326";
    //public static final String IMAGE_CRS = "CRS:1";
    private Set<String> crss;
    private String name;
    private Double numHi;
    private Double numLo;
    private String strHi;
    private String strLo;
    private String type;
    private String uom;
    private Collection<String> allowedAxes;

    public DomainElement(String name, String type, Double numLo, Double numHi, String strLo,
            String strHi, Set<String> crss, Collection<String> axes, String uom) throws WCPSException
            {
        this.allowedAxes = axes;

        if ((name == null) || (type == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    "Invalid domain element: Element name and type cannot be null");
        }

        if (name.equals("")) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    "Invalid domain element: Element name cannot be empty");
        }

        if (allowedAxes.contains(type) == false) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    "Invalid domain element: Invalid element type: " + type
                    + ". Allowed element types are: " + allowedAxes.toString());
        }

        if ((numLo != null) && (numHi != null) && (strLo == null) && (strHi == null)) {
            if (numLo.compareTo(numHi) == 1) {
                throw new WCPSException(ExceptionCode.InvalidMetadata, 
                        "Invalid domain element: Lower integer bound cannot be larger than upper integer bound");
            }

            this.numLo = numLo;
            this.numHi = numHi;
        } else if ((strLo != null) && (numHi != null) && (numLo == null) && (numHi == null)) {
            if (strLo.equals("") || strHi.equals("")) {
                throw new WCPSException(ExceptionCode.InvalidMetadata, 
                        "Invalid domain element: String bounds cannot be empty");
            }

            this.strLo = strLo;
            this.strHi = strHi;
        } else {
            /* Allow both sources of info for time-axes */
            if (type.equals("t")) {
                this.strLo = strLo;
                this.strHi = strHi;
                this.numLo = numLo;
                this.numHi = numHi;
            } else {
                throw new WCPSException(ExceptionCode.InvalidMetadata, 
                        "Invalid domain element: Integer bounds must both be non-null if string bounds are null, and vice versa at "
                        + name + ":" + type);
            }
        }

        if ((type.equals(AxisTypes.X_AXIS) || type.equals(AxisTypes.Y_AXIS)) && (numLo == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, 
                    "Invalid domain element: A spatial axis must have integer extent");
        } else if (type.equals(AxisTypes.T_AXIS) && (numLo == null) || (numHi == null)) {
            throw new WCPSException(ExceptionCode.InvalidMetadata, "Invalid domain element: A \"t\" axis must have integer extent and optionally, string extent");
        }

        this.name = name;
        this.type = type;

        if ((crss == null) || !crss.contains(CrsUtil.IMAGE_CRS)) {
//			throw new WCPSException(ExceptionCode.InvalidMetadata, 
//			    "Invalid domain element: CRS set does not contain image CRS '"
//			    + CrsUtil.IMAGE_CRS + "'");
            crss.add(CrsUtil.IMAGE_CRS);
        }

        this.crss = crss;

    }

    @Override
    public DomainElement clone() {
        Set<String> c = new HashSet<String>(crss.size());
        Iterator<String> i = crss.iterator();

        while (i.hasNext()) {
            c.add(new String(i.next()));
        }

        try {
            String newName = name == null ? null : new String(name);
            String newType = type == null ? null : new String(type);
            Double newNumLo = numLo == null ? null : new Double(numLo);
            Double newNumHi = numHi == null ? null : new Double(numHi);
            String newStrLo = strLo == null ? null : new String(strLo);
            String newStrHi = strHi == null ? null : new String(strHi);
            String newUom = uom == null ? null : new String(uom);
            return new DomainElement(newName, newType, newNumLo, newNumHi, newStrLo, newStrHi, c, allowedAxes, newUom);
        } catch (WCPSException ime) {
            throw new RuntimeException(
                    "Invalid metadata while cloning DomainElement. This is a software bug in WCPS.",
                    ime);
        }

    }

    public boolean equals(DomainElement de) {
        if ((numLo == null) && (de.numLo == null)) {
            return strLo.equals(de.strLo) && strHi.equals(strHi)
                    && name.equals(de.name) && type.equals(de.type);
        } else if ((strLo == null) && (de.strLo == null)) {
            return numLo.equals(de.numLo) && numHi.equals(numHi)
                    && name.equals(de.name) && type.equals(de.type);
        } else {
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public Double getNumHi() {
        return numHi;
    }

    public Double getNumLo() {
        return numLo;
    }

    public String getStrHi() {
        return strHi;
    }

    public String getStrLo() {
        return strLo;
    }

    public String getType() {
        return type;
    }

    public Set<String> getCrsSet() {
        return crss;
    }

    public String getUom() {
        return uom;
    }

    @Override
    public String toString() {
        String d = "Domain Element { Name: '" + name + "', Type: '" + type
                + "', NumLow: '" + numLo + "', NumHi: '" + numHi + "', StrLow: '"
                + strLo + "', StrHi: '" + strHi + "', CrsSet: '" + crss + "'}";
        return d;
    }
}
