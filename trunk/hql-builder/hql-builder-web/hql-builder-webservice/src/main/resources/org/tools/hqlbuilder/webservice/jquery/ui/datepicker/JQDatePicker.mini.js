function initJQDatepicker(e,t,n,r){var i=$.datepicker.regional;i.buttonImage=r,i.dateFormat=n,i.changeMonth=!0,i.changeYear=!0,i.showOn="button",i.buttonImageOnly=!0,$("#"+e).datepicker(i).datepicker("option","disabled",!1)}