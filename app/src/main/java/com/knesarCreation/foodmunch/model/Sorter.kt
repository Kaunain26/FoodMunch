package com.knesarCreation.foodmunch.model

class Sorter {
    companion object {
        var ratingComparator = Comparator<Restaurants> { res1, res2 ->
            if (res1.rating.compareTo(res2.rating, true) == 0) {
                res1.costForOne.compareTo(res2.costForOne)
            } else {
                res1.rating.compareTo(res2.rating, true)
            }
        }

        var costComparator = Comparator<Restaurants> { res1, res2 ->
            if (res1.costForOne.compareTo(res2.costForOne, true) == 0) {
                res1.rating.compareTo(res2.rating)
            } else {
                res1.costForOne.compareTo(res2.costForOne, true)
            }
        }
    }
}