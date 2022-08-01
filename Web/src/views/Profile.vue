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
              <th v-if="parseInt('' + turno.weekyear) === parseInt('' + week)">
                {{ index }}
              </th>
            </template>
          </thead>
          <tbody>
            <tr v-for="turno of piv" :key="turno.id">
              <template
                v-for="value in turno"
                :key="value"
                style="text-align: center"
              >
                <td
                  v-if="parseInt('' + value.weekyear) === parseInt('' + week)"
                >
                  <div v-if="typeof value.checked === 'undefined'">
                    {{ value.id }}
                  </div>
                  <button
                    type="button"
                    class="btn"
                    v-bind:class="{
                      'btn-danger':
                        value.checked === 'false' || value.checked === false,
                      'btn-success':
                        value.checked === 'true' || value.checked === true,
                    }"
                    v-if="typeof value.checked !== 'undefined'"
                    :disabled="user.name !== value.id"
                    @click="value.checked = toggle(value.checked)"
                  >
                    {{
                      value.checked === "true" || value.checked === true
                        ? "✓"
                        : "-"
                    }}
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
import axios from "axios";

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
    pivot: function (turni) {
      let dict = {};
      turni.forEach(function (turno) {
        dict[turno.id + " " + turno.weekyear] = (dict[
          turno.id + " " + turno.weekyear
        ]
          ? dict[turno.id + " " + turno.weekyear]
          : [{ id: turno.id, weekyear: turno.weekyear }]
        ).concat([
          {
            slotdate: turno.slotdate,
            slotbin: turno.slotbin,
            checked: turno.checked,
            weekyear: turno.weekyear,
            modified: turno.modified,
            id: turno.id,
          },
        ]);
      });
      return dict;
    },
    toggle(value) {
      if (value === true || value === "true") {
        return false;
      } else {
        return true;
      }
    },
    header: function (turni) {
      let dict = {};
      turni.forEach(function (turno) {
        dict[turno.slotdate + " " + turno.slotbin] = {
          slotdate: turno.slotdate,
          slotbin: turno.slotbin,
          checked: turno.checked,
          weekyear: turno.weekyear,
          modified: turno.modified,
          id: turno.id,
        };
      });
      return dict;
    },
  },
  data() {
    return {
      week: moment().week(),
      turni: [],
      piv: [],
    };
  },
  mounted() {
    axios
      .get("http://localhost:8080/Gradle___db_population_war/MyDoodle")
      .then((response) => {
        this.turni = response["data"];
        this.piv = this.pivot(this.turni);
      });
  },
};
</script>

