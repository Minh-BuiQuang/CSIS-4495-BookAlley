using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace BookAlleyWebApi.Migrations
{
    /// <inheritdoc />
    public partial class mssqlazure_migration_191 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "Note",
                table: "Posts",
                type: "nvarchar(max)",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Note",
                table: "Posts");
        }
    }
}
