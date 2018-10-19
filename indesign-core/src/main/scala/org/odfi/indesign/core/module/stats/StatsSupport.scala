package org.odfi.indesign.core.module.stats

trait StatsSupport extends StatsSupportTrait {

    // Update Definition
    //-----------
    def updateStats = {

    }

    //def saveStat

    // Define Stats
    //--------------

    /**
     * Define a st
     */
    def saveStat(id: String, value: String, vtype: String, history: Boolean = false): Unit = {
        findStat(id) match {
            case Some(st) if (history) =>

                //-- Save history
                var hist = st.histories.add
                hist.savedDate = st.lastUpdated
                hist.value = st.value

                //-- Save Value
                st.value = value

            case Some(st) =>

                //-- Save Value
                st.value = value

            case None =>

                var st = this.stats.stats.add
                st.eid = id
                st.dataType = vtype
                st.value = value

        }
    }

    def saveStringStat(id: String, value: String, history: Boolean): Unit = {
        this.saveStat(id, value, "string", history)
    }
    def saveIntStat(id: String, value: Int, history: Boolean): Unit = {
        this.saveStat(id, value.toString, "int", history)
    }

    // Find Stats
    //------------------

    def findStat(id: String) = {
        this.stats.stats.find {
            st =>
                st.eid.toString() == id
        }
    }

    def getStat(id: String, default: String): String = {
        findStat(id) match {
            case Some(s) => s.value
            case None    => default
        }
    }
}