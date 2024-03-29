#!/bin/bash
#!/bin/ksh
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#	test.sh
# Description
#	Command-line utility for testing rasdaman.
#	1)creating collection
# 	2)insert images into collection
# 	3)extract images 
# 	4)compare
# 	5)cleanup 
#
# PRECONDITIONS
# 	1)Postgres Server must be running
# 	2)Rasdaman Server must be running
# 	3)database RASBASE must exists
# 	4)rasql utility must be fully running
# 	5)images needed for testing shall be put in directory of images 	
# Usage: ./test.sh 
#        
# CHANGE HISTORY
#       2009-Sep-16     J.Yu       created
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

#
# paths
#
TESTDATA_PATH="$SCRIPT_DIR/testdata"
[ -d "$TESTDATA_PATH" ] || error "Testdata directory not found: $TESTDATA_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Expected results directory not found: $ORACLE_PATH"





# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman
check_gdal

# check data types
check_type GreySet
check_type RGBSet
check_user_type TestSet



# ------------------------------------------------------------------------------
# drop collection if they already exists
#
drop_colls test_tmp

# ------------------------------------------------------------------------------
# function runnning the test
#
function run_test()
{
local fun=$1
local inv_fun=$2
local ext=$3
local inv_ext=$4
local colltype=$5
local f="mr_1"
if [ "$colltype" == RGBSet ]; then
  f=rgb
elif [ "$colltype" == TestSet ]; then
  f=multiband
fi
local extraopts="$6"

log ----- $fun and inv_$fun conversion ------

create_coll test_tmp $colltype
insert_into test_tmp "$TESTDATA_PATH/$f.$inv_ext" "$extraopts" "inv_$inv_fun"
export_to_file test_tmp "$f" "$fun"

logn "comparing images: "
if [ -f "$ORACLE_PATH/$f.$ext.checksum" ]; then
  $GDALINFO $f.$ext | grep 'Checksum' > $f.$ext.result
  diff $ORACLE_PATH/$f.$ext.checksum $f.$ext.result > /dev/null
else
  cmp $TESTDATA_PATH/$f.$ext $f.$ext > /dev/null
fi

if [ $? != "0" ]
then
  echo input and output do not match
  NUM_FAIL=$(($NUM_FAIL + 1))
else
  echo input and output match
  NUM_SUC=$(($NUM_SUC + 1))
fi

drop_colls test_tmp
rm -f $f*
}

################## test nodata ###############################

create_coll test_tmp RGBSet
insert_into test_tmp "$TESTDATA_PATH/rgb.png" "" "inv_png"

$RASQL -q 'select encode(c, "GTiff") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=0" | wc -l`
if [ $res -eq 3 ]; then
  log "default nodata value test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "default nodata value test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
rm -f nodata*
$RASQL -q 'select encode(c, "GTiff", "nodata=200") from test_tmp as c' --out file --outfile nodata > /dev/null
res=`gdalinfo nodata.tif | grep "NoData Value=200" | wc -l`
if [ $res -eq 3 ]; then
  log "custom nodata value test passed."
  NUM_SUC=$(($NUM_SUC + 1))
else
  log "custom nodata value test failed."
  NUM_FAIL=$(($NUM_FAIL + 1))
fi
rm -f nodata*

drop_colls test_tmp

################## jpeg() and inv_jpeg() #######################
run_test jpeg jpeg jpg jpg GreySet

################## tiff() and inv_tiff() #######################
run_test tiff tiff tif tif GreySet
run_test tiff tiff tif tif TestSet ", \"sampletype=octet\""

################## png() and inv_png() #######################
run_test png png png png GreySet

################## bmp() and inv_bmp() #######################
run_test bmp bmp bmp bmp GreySet

################## vff() and inv_vff() #######################
run_test vff vff vff vff GreySet

################## hdf() and inv_hdf() #######################

# run hdf test only if hdf was compiled in
grep 'HAVE_HDF 1' $SCRIPT_DIR/../../../config.h

if [ $? -eq 0 ]; then
  run_test hdf hdf hdf hdf GreySet
fi

################## csv() #######################

run_test csv png csv png GreySet
run_test csv png csv png RGBSet

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
