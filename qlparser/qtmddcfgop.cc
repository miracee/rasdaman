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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/*************************************************************
 *
 * CHANGE HISTORY (append further entries):
 * when         who             what
 * ----------------------------------------------------------
 * 10-11-08     Shams      Created the class
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include "qlparser/qtmddcfgop.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtmddconfig.hh"
#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif
#include<fstream>



QtMddCfgOp::QtMddCfgOp()
    :  QtOperation(),
       input(NULL),
       mddCfgObj(NULL)
{
}


QtMddCfgOp::QtMddCfgOp( QtOperation* inputInit )
    :  QtOperation(),
       input( inputInit ),
       mddCfgObj(NULL)
{
    if( input )
        input->setParent( this );
}

QtMddCfgOp::QtMddCfgOp( int tilingType, int tileSize, int borderThreshold,
                        double interestThreshold, QtOperation* tileCfg, QtNode::QtOperationList* box,std::vector<r_Dir_Decompose>* dDecomp,
                        int indexType)
    :  QtOperation(),
       input( NULL )
{
    mddCfgObj = new QtMDDConfig(tilingType,tileSize, borderThreshold, interestThreshold, tileCfg, box, dDecomp, indexType);

}

QtMddCfgOp::QtMddCfgOp( int tilingType, int tileSize, int borderThreshold,
                        double interestThreshold, QtOperation* tileCfg, QtNode::QtOperationList* box,std::vector<r_Dir_Decompose>* dDecomp)
    :  QtOperation(),
       input( NULL )
{
    mddCfgObj = new QtMDDConfig(tilingType,tileSize, borderThreshold, interestThreshold, tileCfg, box, dDecomp, QtMDDConfig::r_DEFAULT_INDEX);

}

QtMddCfgOp::QtMddCfgOp(int index)
    :  QtOperation(),
       input( NULL )
{
    mddCfgObj = new QtMDDConfig(QtMDDConfig::r_DEFAULT_TLG, -1, -1, -1, NULL, NULL,NULL,index);

}

QtMddCfgOp::~QtMddCfgOp()
{
    if( input )
    {
        delete input;
        input=NULL;
    }
    if ( mddCfgObj )
    {
        delete mddCfgObj;
        mddCfgObj=NULL;
    }
}


void
QtMddCfgOp::optimizeLoad( QtTrimList* trimList )
{
    RMDBCLASS( "QtMddCfgOp", "optimizeLoad( QtTrimList* )", "qlparser", __FILE__, __LINE__ )

    // by default, pass load domain to the input
    if( input )
        input->optimizeLoad( trimList );
    else
    {
        delete trimList;
        trimList=NULL;
    }
}

QtMDDConfig*
QtMddCfgOp::evaluate(QtDataList* inputList)
{
    QtMDDConfig* retvalue = NULL;
    for (int i = 0 ; i < inputList->size() ; i++)
    {
        retvalue = (QtMDDConfig*)inputList->at(i);
    }
    return retvalue;
}

QtMDDConfig*
QtMddCfgOp::getMddConfig()
{
    return mddCfgObj;
}
