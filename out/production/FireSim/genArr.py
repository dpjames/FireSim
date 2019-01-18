from osgeo import gdal
import json
#################################
def toJSON(arr):
    js = "["
    for row in arr:
        js+=json.dumps(row.tolist()) + ",\n"
    js = js[0:len(js) - 2] + "]"
    return js

ds = gdal.Open("in.tif")
arr =ds.GetRasterBand(1).ReadAsArray()
print toJSON(arr)
