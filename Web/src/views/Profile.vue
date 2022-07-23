<template>
  <div class="container">
    <div class="row">
      <div class="container d-flex">
        <button type="button" class="btn btn-light" @click="week = week - 1">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            class="bi bi-arrow-left-circle"
            viewBox="0 0 16 16"
          >
            <path
              fill-rule="evenodd"
              d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zm-4.5-.5a.5.5 0 0 1 0 1H5.707l2.147 2.146a.5.5 0 0 1-.708.708l-3-3a.5.5 0 0 1 0-.708l3-3a.5.5 0 1 1 .708.708L5.707 7.5H11.5z"
            ></path>
          </svg>
        </button>
        <table class="table table-striped table-bordered">
          <thead>
            <th></th>
            <template
              v-for="[index, turno] of Object.entries(header(turni))"
              :key="turno"
              style="text-align: center"
            >
              <th v-if="turno.week == week">{{ index }}</th>
            </template>
          </thead>
          <tbody>
            <tr v-for="turno of piv" :key="turno.name">
              <template
                v-for="value in turno"
                :key="value"
                style="text-align: center"
              >
                <td v-if="value.week == week">
                  <div v-if="typeof value.checked === 'undefined'">
                    {{ value.name }}
                  </div>
                  <button
                    type="button"
                    class="btn"
                    v-bind:class="{
                      'btn-danger': value.checked === false,
                      'btn-success': value.checked === true,
                    }"
                    v-if="typeof value.checked !== 'undefined'"
                    :disabled="user.name !== value.name"
                    @click="value.checked = !value.checked"
                  >
                    {{ value.checked ? "✓" : "-" }}
                  </button>
                </td>
              </template>
            </tr>
          </tbody>
        </table>
        <button type="button" class="btn btn-light" @click="week = week + 1">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            fill="currentColor"
            class="bi bi-arrow-right-circle"
            viewBox="0 0 16 16"
          >
            <path
              fill-rule="evenodd"
              d="M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8zm15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM4.5 7.5a.5.5 0 0 0 0 1h5.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3a.5.5 0 0 0 0-.708l-3-3a.5.5 0 1 0-.708.708L10.293 7.5H4.5z"
            ></path>
          </svg>
        </button>
      </div>
      <div class="container d-flex justify-content-center">
        <button type="button" class="btn btn-primary" style="align: center">
          Save
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuth0 } from "@auth0/auth0-vue";
import moment from "moment";
export default {
  modified: false,
  name: "profile",
  setup() {
    const auth0 = useAuth0();
    return {
      user: auth0.user,
    };
  },
  methods: {
    print(value) {
      return true;
    },
    toggle(value) {
      value.checked = !value.checked;
    },
    pivot: function (turni) {
      let dict = {};
      turni.forEach(function (turno) {
        console.log(turno.name)
        dict[turno.name + " " + turno.week] = (dict[turno.name + " " + turno.week]
          ? dict[turno.name + " " + turno.week]
          : [{ name: turno.name, week: turno.week }]
        ).concat([
          {
            date: turno.date,
            turno: turno.turno,
            checked: turno.checked,
            week: turno.week,
            modified: turno.modified,
            name: turno.name
          },
        ]);
      });
      return dict;
    },
    header: function (turni) {
      let dict = {};
      turni.forEach(function (turno) {
        dict[turno.date + " " + turno.turno] = {
          date: turno.date,
          turno: turno.turno,
          checked: turno.checked,
          week: turno.week,
          modified: turno.modified,
          name: turno.name,
        };
      });
      return dict;
    },
  },
  data() {
    return {
      week: moment("2022-07-08", "YYYY-MM-DD").week(),
      turni: [
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          week: 28,
          checked: true,
        },
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          week: 28,
          checked: false,
        },
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          week: 29,
          checked: false,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          week: 28,
          checked: true,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          week: 28,
          checked: false,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          week: 29,
          checked: false,
        },
      ],
      piv: this.pivot([
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          week: 28,
          checked: true,
        },
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          week: 28,
          checked: false,
        },
        {
          modified: false,
          name: "teo.francia@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          week: 29,
          checked: false,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          week: 28,
          checked: true,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          week: 28,
          checked: false,
        },
        {
          modified: false,
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          week: 29,
          checked: false,
        },
      ]),
    };
  },
};
</script>

