<template>
  <div class="container">
    <div class="row align-items-center profile-header">
      <div class="col-md-2 mb-3">
        <img
          :src="user?.picture"
          alt="User's profile picture"
          class="rounded-circle img-fluid profile-picture"
        />
      </div>
      <div class="col-md text-center text-md-left">
        <h2>{{ user?.name }}</h2>
      </div>
    </div>

    <div class="row">
      <div class="container">
        <table class="table table-striped table-bordered">
          <thead>
            <th></th>
            <th
              v-for="[index, turno] of Object.entries(header(turni))"
              :key="turno"
              style="text-align: center"
            >
              {{ index }}
            </th>
          </thead>
          <tbody>
            <tr v-for="turno of piv" :key="turno.name">
              <td
                v-for="value in turno"
                :key="value"
                style="text-align: center"
              >
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
                  :disabled="user.name !== value.name"
                  @click="value.checked = !value.checked"
                >
                  {{ value.checked ? "x" : "-" }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuth0 } from "@auth0/auth0-vue";

export default {
  name: "profile",
  setup() {
    const auth0 = useAuth0();
    return {
      user: auth0.user,
    };
  },
  methods: {
    print(value) {
      console.log(value);
      return true;
    },
    toggle(value) {
      console.log(value);
      value.checked = !value.checked;
    },
    pivot: function (turni) {
      let dict = {};
      turni.forEach(function (turno) {
        dict[turno.name] = (dict[turno.name]
          ? dict[turno.name]
          : [{ name: turno.name }]
        ).concat([
          {
            date: turno.date,
            turno: turno.turno,
            checked: turno.checked,
            name: turno.name,
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
        };
      });
      return dict;
    },
  },
  data() {
    return {
      turni: [
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          checked: true,
        },
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          checked: true,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          checked: false,
        },
      ],
      piv: this.pivot([
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          checked: true,
        },
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "teo.francia@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED AM",
          checked: true,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-07",
          turno: "PED PM",
          checked: false,
        },
        {
          name: "laura.tarsi14@gmail.com",
          date: "2022-07-08",
          turno: "PED PM",
          checked: false,
        },
      ]),
    };
  },
};
</script>

